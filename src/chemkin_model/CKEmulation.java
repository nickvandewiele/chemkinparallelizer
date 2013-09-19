package chemkin_model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import parsers.ConfigurationInput;
import readers.ReactorInput;
import util.ChemkinConstants;
import util.Paths;
import util.Tools;
import datamodel.ModelValueFactory;
/**
 * CKEmulation is designed as a Thread type, implying that multiple CKEmulations can be initiated, allowing multithreading and possible speed-up<BR>
 * CKEmulation can call several Chemkin routines: Chem, CKReactorPlugFlow, GetSolution, GetSolnTranspose depending on the task to be executed<BR>
 * In order to cope with a limited number of Chemkin licenses, a counting Semaphore type is used to keep track of the number of licenses in use<BR>
 *  
 * @author nmvdewie
 *
 */
public class CKEmulation extends AbstractCKEmulation{
	private static final String ERROR_THERE_IS_AN_ERROR_IN_THE_TRANSPORT_LINKFILE = "ERROR...THERE IS AN ERROR IN THE TRANSPORT LINKFILE";
	static Logger logger = Logger.getLogger(CKEmulation.class);


	//CONSTRUCTORS:
	//constructor for checking validity of chemistry input file:
	public CKEmulation(ConfigurationInput config){
		super.config = config;
	}

	public CKEmulation(ConfigurationInput config, ReactorInput reactorInput) {

		this(config);

		this.reactorInput = reactorInput;
		int length = reactorInput.filename.length();
		this.reactorOut = reactorInput.filename.substring(0,(length-4))+".out";
		this.reactorDir = config.paths.getWorkingDir()+"temp_"+reactorInput.filename.substring(0,(length-4))+"/";

		boolean temp = new File(reactorDir).mkdir();
		if(!temp){
			logger.debug("Creation of reactor directory failed!");
			System.exit(-1);
		}
		//reactor setup:
		Tools.copyFile(config.paths.getWorkingDir()+getReactorInput().filename,getReactorDir()+getReactorInput().filename);

		ModelValueFactory factory = new ModelValueFactory(getReactorInput().type);
		this.modelValue = factory.createModelValue();

		try {
			copyLinkFiles(config.paths);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void copyLinkFiles(Paths paths) throws Exception {
		//copy chemistry and transport link files:
		if(new File(paths.getWorkingDir()+ChemkinConstants.CHEMASC).exists()){
			Tools.copyFile(paths.getWorkingDir()+ChemkinConstants.CHEMASC,getReactorDir()+ChemkinConstants.CHEMASC);	
		}
		else throw new Exception("Could not find chem link file!");

		if(new File(paths.getWorkingDir()+ChemkinConstants.TRANASC).exists()){
			if(checkTranOutput(paths)){
				Tools.copyFile(paths.getWorkingDir()+ChemkinConstants.TRANASC,getReactorDir()+ChemkinConstants.TRANASC);	
			}	
		}
		else throw new Exception("Could not find tran link file!");
	}
	/**
	 * run() is the method that will be executed when Thread.start() is executed.
	 * Its argument list is void (mandatory I think).
	 */
	@Override
	public void run(){

	}


	/**
	 * Checks if errors are present in the transport output file. If so, this means that either:
	 * -no transport data was present in chemistry input file
	 * -something went wrong with processing the transport data
	 * @param paths TODO
	 * @return false if errors is found in transport file, if not, returns true
	 */
	public boolean checkTranOutput(Paths paths){
		File file = new File(paths.getWorkingDir()+ChemkinConstants.TRANOUT);
		boolean ok = false;
		try {
			ok = !FileUtils.readFileToString(file).contains(ERROR_THERE_IS_AN_ERROR_IN_THE_TRANSPORT_LINKFILE);
		} catch (IOException e1) {}
		return ok;

	}

	/**
	 * ####################
	 * GETTERS AND SETTERS:
	 * ####################
	 */

}




