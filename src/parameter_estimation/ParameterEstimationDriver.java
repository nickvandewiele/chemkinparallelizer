package parameter_estimation;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import parsers.ConfigurationInput;
import parsers.XMLInputParser;

public class ParameterEstimationDriver {
	public static Logger logger = Logger.getLogger(ParameterEstimationDriver.class);
	/**
	 * @param args
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		initializeLog();

		BufferedReader in  = null;
		in = readINPUT(args);
		
		XMLInputParser read = new XMLInputParser();
		//assume INPUT.xml is passed in as command line argument
		List<ConfigurationInput> readConfig = read.readConfig(args[0]);
		for (ConfigurationInput config : readConfig){
		
			//creation of the Receiver object that will perform the actual work:
			/**
			 * The entire configuration object is passed to the Param_Est constructor
			 */
			Param_Est param_est = new Param_Est(config);
			
			//creation of the invoker object:
			ParamEstimationInvoker invoker = new ParamEstimationInvoker();

			//parity command creation and binding to invoker:
			Command parityCommand = new ParityPlotCommand(param_est);
			invoker.setCommand(0, parityCommand);
			
			//kinetic parameters optimization command creation and binding to invoker:
			Command optimCommand = new OptimizationCommand(param_est);
			invoker.setCommand(1, optimCommand);

			//excel postprocessing command creation and binding to invoker:
			Command excelCommand = new ExcelPostProcessingCommand(param_est);
			invoker.setCommand(2, excelCommand);
			
			//statistics command creation and binding to invoker:
			Command statisticsCommand = new OptimizationCommand(param_est);
			invoker.setCommand(3, statisticsCommand);
			
			//perform the request:
			invoker.performMode(config.getMODE());
		}
		
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this program to finish: (sec) "+timeTook);
	}

	private static BufferedReader readINPUT(String[] args)
	throws FileNotFoundException {
		BufferedReader in;
		if(args.length == 0){
			//input file will be searched in working directory under the name INPUT.txt:
			in = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/INPUT.txt"));	
		}
		else {
			//specify user-defined input file:
			in = new BufferedReader(new FileReader(args[0]));
		}
		return in;
	}

	
	public static void initializeLog(){
		/*	
		Layout layout = new SimpleLayout();

		//make Appender, it's a FileAppender, writing to NBMT.log:
		FileAppender appender = null;
		try {
			appender = new FileAppender(layout, "NBMT.log", false);
		} catch(Exception e) {}

		//add Appender:
		logger.addAppender(appender);
		 */	
		BasicConfigurator.configure();
	}
}
