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
		
		XMLInputParser read = new XMLInputParser();
		//assume INPUT.xml is passed in as command line argument
		List<ConfigurationInput> readConfig = read.readConfig(args[0]);
		for (ConfigurationInput config : readConfig){
		
			//creation of the invoker object:
			ParamEstimationInvoker invoker = new ParamEstimationInvoker();

			//parity command creation and binding to invoker:
			Command parityCommand = new ParityPlotCommand(config);
			invoker.setCommand(0, parityCommand);
			
			//kinetic parameters optimization command creation and binding to invoker:
			Command optimCommand = new OptimizationCommand(config);
			invoker.setCommand(1, optimCommand);

			//excel postprocessing command creation and binding to invoker:
			Command excelCommand = new ExcelPostProcessingCommand(config);
			invoker.setCommand(2, excelCommand);
			
			//statistics command creation and binding to invoker:
			Command statisticsCommand = new OptimizationCommand(config);
			invoker.setCommand(3, statisticsCommand);
			
			//perform the request:
			invoker.performMode(config.getMODE());
		}
		
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this program to finish: (sec) "+timeTook);
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
