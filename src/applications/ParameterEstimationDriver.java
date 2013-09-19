package applications;
import java.io.File;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.google.common.base.StandardSystemProperty;

import commands.Command;
import commands.ExcelPostProcessingCommand;
import commands.OptimizationCommand;
import commands.ParamEstimationInvoker;
import commands.ParityPlotCommand;

import parsers.ConfigurationInput;
import parsers.XMLInputParser;
import readers.ExperimentalDatabaseInput;
import readers.ReactorSetupInput;
import util.Paths;

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
		System.out.println("Reading from: "+StandardSystemProperty.USER_DIR.value());
		initializeLog();
		flagUseMassFractions = args[0].equals("--mass");
		
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
		boolean temp = new File(Paths.getOutputDir()).mkdir();
		if(!temp){
			logger.debug("Creation of output directory failed!");
			System.exit(-1);
		}
		if(!new File(Paths.getChemkinDir()).exists()){
			logger.error("Chemkin folder not found!");
			System.exit(-1);
		}
		
		if(!new File(Paths.getUDROPDir()).exists()){
			logger.error("UDROP Folder not found!");
			System.exit(-1);
		}
		
		if(!new File(Paths.getWorkingDir(),Paths.chemistryInput).exists()){
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
			if(!new File(Paths.getWorkingDir(),rsi.getLocation()).exists()){
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
