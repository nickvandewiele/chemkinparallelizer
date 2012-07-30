package parameter_estimation;
import java.io.File;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import parsers.ConfigurationInput;
import parsers.XMLInputParser;
import readers.ExperimentalDatabaseInput;
import readers.ReactorSetupInput;

public class ParameterEstimationDriver {
	public static Logger logger = Logger.getLogger(ParameterEstimationDriver.class);
	public static Boolean flagUseMassFractions = null;
	
	/**
	 * @param args
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		initializeLog();
		
		flagUseMassFractions = args[0].equals("--mass") ? true : false;
		
		XMLInputParser read = new XMLInputParser();
		//assume INPUT.xml is passed in as command line argument
		List<ConfigurationInput> readConfig = read.readConfig(args[1]);
		for (ConfigurationInput config : readConfig){
			checkPaths(config);
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


	private static void checkPaths(ConfigurationInput config) {

		if(!new File(config.paths.chemkinDir).exists()){
			logger.error("Chemkin folder not found!");
			System.exit(-1);
		}
		if(!new File(config.paths.workingDir).exists()){
			logger.error("Working directory not found!");
			System.exit(-1);
		}
		if(!config.paths.UDROPDir.exists()){
			logger.error("UDROP Folder not found!");
			System.exit(-1);
		}
		if(!new File(config.paths.workingDir,config.chemistry.getChemistryInput()).exists()){
			logger.error("Chemistry Input not found!");
			System.exit(-1);
		}
		if(!new File(config.experiments.getPathCSTRTemplate()).exists()){
			logger.error("CSTRTemplate not found!");
			System.exit(-1);
		}
		if(!new File(config.experiments.getPathPFRTemplate()).exists()){
			logger.error("PFRTemplate not found!");
			System.exit(-1);
		}
		for(ReactorSetupInput rsi: config.reactor_setup){
			if(!new File(config.paths.workingDir,rsi.getLocation()).exists()){
				logger.error(rsi.getLocation()+" not found!");
				System.exit(-1);
			}
		}
		if(config.experiments.exp_db != null){
			for(ExperimentalDatabaseInput edi: config.experiments.exp_db){
				if(!edi.location.exists()){
					logger.error(edi.location+" not found!");
					System.exit(-1);
				}
			}
		}
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
