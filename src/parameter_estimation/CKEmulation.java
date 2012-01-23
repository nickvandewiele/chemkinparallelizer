package parameter_estimation;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import parsers.ConfigurationInput;

import chemkin_wrappers.AbstractChemkinRoutine;
import chemkin_wrappers.BatchDecorator;
import chemkin_wrappers.CSTRDecorator;
import chemkin_wrappers.ChemkinRoutine;
import chemkin_wrappers.CreateSolnListDecorator;
import chemkin_wrappers.GetSolutionDecorator;
import chemkin_wrappers.LaminarFlameDecorator;
import chemkin_wrappers.PFRDecorator;
import chemkin_wrappers.PreProcessDecorator;
import chemkin_wrappers.PremixedFlameDecorator;
import datatypes.ModelValueFactory;

import readers.ReactorInput;
import readers.ReactorSetupInput;
/**
 * CKEmulation is designed as a Thread type, implying that multiple CKEmulations can be initiated, allowing multithreading and possible speed-up<BR>
 * CKEmulation can call several Chemkin routines: Chem, CKReactorPlugFlow, GetSolution, GetSolnTranspose depending on the task to be executed<BR>
 * In order to cope with a limited number of Chemkin licenses, a counting Semaphore type is used to keep track of the number of licenses in use<BR>
 *  
 * @author nmvdewie
 *
 */
public class CKEmulation extends AbstractCKEmulation{
	static Logger logger = Logger.getLogger(CKEmulation.class);


	//CONSTRUCTORS:
	//constructor for checking validity of chemistry input file:
	public CKEmulation(ConfigurationInput config){
		super.config = config;
	}

	public CKEmulation(ConfigurationInput config, Runtime rt,
			ReactorInput reactorInput) {

		this(config);

		super.reactorInput = reactorInput;
		int length = reactorInput.filename.length();
		super.reactorOut = reactorInput.filename.substring(0,(length-4))+".out";
		super.reactorDir = config.paths.getWorkingDir()+"temp_ "+reactorInput.filename.substring(0,(length-4))+"/";

		boolean temp = new File(reactorDir).mkdir();
		if(!temp){
			logger.debug("Creation of reactor directory failed!");
			System.exit(-1);
		}
		//reactor setup:
		Tools.copyFile(config.paths.getWorkingDir()+getReactorInput().filename,getReactorDir()+getReactorInput().filename);

		ModelValueFactory factory = new ModelValueFactory(getReactorInput().type);
		super.modelValue = factory.createModelValue();

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
			//copy only if tran output file returns no errors:
			BufferedReader in = new BufferedReader(new FileReader(paths.getWorkingDir()+ChemkinConstants.TRANOUT));
			if(checkTranOutput(in)){
				Tools.copyFile(paths.getWorkingDir()+ChemkinConstants.TRANASC,getReactorDir()+ChemkinConstants.TRANASC);	
			}	
		}
		else throw new Exception("Could not find tran link file!");
	}
	/**
	 * run() is the method that will be executed when Thread.start() is executed.
	 * Its argument list is void (mandatory I think).
	 */
	public void run(){

	}


	/**
	 * Checks if errors are present in the transport output file. If so, this means that either:
	 * -no transport data was present in chemistry input file
	 * -something went wrong with processing the transport data
	 * @param in TODO
	 * @return false if errors is found in transport file, if not, returns true
	 */
	public boolean checkTranOutput(BufferedReader in){
		boolean flag = true;
		try {
			String dummy = in.readLine();
			while(flag&&(!dummy.equals(null))){
				if (dummy.trim().equals("ERROR...THERE IS AN ERROR IN THE TRANSPORT LINKFILE")){
					flag = false;						
				}
				dummy = in.readLine();
			}
			in.close();
		} catch(Exception e){}

		return flag;

	}

	/**
	 * ####################
	 * GETTERS AND SETTERS:
	 * ####################
	 */

}




