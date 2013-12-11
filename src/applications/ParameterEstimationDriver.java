package applications;
import java.io.File;

import org.apache.commons.io.FileUtils;
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
import util.ChemkinConstants;
import util.Paths;

public class ParameterEstimationDriver {
	private static final String REQUIRED_FILES = "requiredFiles";
	public static Logger logger = Logger.getLogger(ParameterEstimationDriver.class);
	/**
	 * @param args
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		System.out.println("Reading from: "+StandardSystemProperty.USER_DIR.value());	
		initializeLog();

		//assume INPUT.xml is passed in as command line argument
		XMLInputParser.readConfig(args[0]);

		checkPaths();
		
		//copy chemkindata.dtd from required files folder to home directory:
		File homedir = new File(StandardSystemProperty.USER_DIR.value());
		File required = new File(Paths.EXEC_LOCATION,REQUIRED_FILES);
		FileUtils.copyFile(new File(required, ChemkinConstants.CHEMKINDATADTD), new File(homedir, ChemkinConstants.CHEMKINDATADTD));
		
		//creation of the invoker object:
		ParamEstimationInvoker invoker = new ParamEstimationInvoker();

		//parity command creation and binding to invoker:
		Command parityCommand = new ParityPlotCommand();
		invoker.setCommand(0, parityCommand);

		//kinetic parameters optimization command creation and binding to invoker:
		Command optimCommand = new OptimizationCommand();
		invoker.setCommand(1, optimCommand);

		//excel postprocessing command creation and binding to invoker:
		Command excelCommand = new ExcelPostProcessingCommand();
		invoker.setCommand(2, excelCommand);

		//statistics command creation and binding to invoker:
		Command statisticsCommand = new OptimizationCommand();
		invoker.setCommand(3, statisticsCommand);

		//perform the request:
		invoker.performMode(ConfigurationInput.getMODE());


		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this program to finish: (sec) "+timeTook);
		
		//delete chemkindata.dtd file in home directory:
		FileUtils.deleteQuietly(new File(homedir, ChemkinConstants.CHEMKINDATADTD));
	}


	private static void checkPaths() {
		boolean temp = new File(Paths.getOutputDir()).mkdir();
		if(!temp){
			logger.debug("Creation of output directory failed!");
			System.exit(-1);
		}
		if(!Paths.getChemkinDir().exists()){
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
		for(ReactorSetupInput rsi: ConfigurationInput.reactor_setup){
			if(!new File(Paths.getWorkingDir(),rsi.getLocation()).exists()){
				logger.error(rsi.getLocation()+" not found!");
				System.exit(-1);
			}
		}
		if(ConfigurationInput.experiments.exp_db != null){
			for(ExperimentalDatabaseInput edi: ConfigurationInput.experiments.exp_db){
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
