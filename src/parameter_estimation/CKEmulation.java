package parameter_estimation;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import chemkin_wrappers.AbstractChemkinRoutine;
import chemkin_wrappers.BatchDecorator;
import chemkin_wrappers.CSTRDecorator;
import chemkin_wrappers.ChemkinRoutine;
import chemkin_wrappers.ChemkinRoutines;
import chemkin_wrappers.CreateSolnListDecorator;
import chemkin_wrappers.GetSolutionDecorator;
import chemkin_wrappers.LaminarFlameDecorator;
import chemkin_wrappers.PFRDecorator;
import chemkin_wrappers.PreProcessDecorator;
import chemkin_wrappers.PremixedFlameDecorator;

import readers.ConfigurationInput;
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
	public CKEmulation(ConfigurationInput config, Runtime runtime){
		this.config = config;
		this.experiment = new Experiment();
		this.effluent = new Effluent();
		this.runtime = runtime;
		
	}

	//constructor for creating CKSolnList.txt
	public CKEmulation(ConfigurationInput input, Runtime runtime,
			String reactorSetup, boolean first){
		this(input, runtime);
		this.reactorSetup = reactorSetup;
		this.first = first;
	}

	//constructor for running 'classical' Chemkin routines
	public CKEmulation(ConfigurationInput input, Runtime runtime,
			String rs, Semaphore s, boolean flagCKSolnList, boolean flagExcel, 
			boolean flagIgnitionDelay, boolean flagFlameSpeed) throws Exception{
		this(input, runtime, rs, flagCKSolnList);
		int length = rs.length();
		this.reactorOut = rs.substring(0,(length-4))+".out";

		this.flagExcel = flagExcel;
		this.flagIgnitionDelayExperiment = flagIgnitionDelay;
		this.flagFlameSpeedExperiment = flagFlameSpeed;
		this.semaphore = s;

		this.reactorDir = config.paths.getWorkingDir()+"temp_ "+rs.substring(0,(length-4))+"/";
		boolean temp = new File(reactorDir).mkdir();
		if(!temp){
			logger.debug("Creation of reactor directory failed!");
			System.exit(-1);
		}


		copyLinkFiles(config.paths);

	}

	private void copyLinkFiles(Paths paths) throws Exception {
		//copy chemistry and transport link files:
		if(new File(paths.getWorkingDir()+ChemkinConstants.CHEMASC).exists()){
			Tools.copyFile(paths.getWorkingDir()+ChemkinConstants.CHEMASC,reactorDir+ChemkinConstants.CHEMASC);	
		}
		else throw new Exception("Could not find chem link file!");

		if(new File(paths.getWorkingDir()+ChemkinConstants.TRANASC).exists()){
			//copy only if tran output file returns no errors:
			BufferedReader in = new BufferedReader(new FileReader(paths.getWorkingDir()+ChemkinConstants.TRANOUT));
			if(checkTranOutput(in)){
				Tools.copyFile(paths.getWorkingDir()+ChemkinConstants.TRANASC,reactorDir+ChemkinConstants.TRANASC);	
			}	
		}
		else throw new Exception("Could not find tran link file!");
	}
	/**
	 * run() is the method that will be executed when Thread.start() is executed.
	 * Its argument list is void (mandatory I think).
	 */
	public void run(){
		try {
			semaphore.acquire();

			logger.info("license acquired!"+reactorSetup);

			//copy chemistry input to the reactorDir:
			Tools.copyFile(config.paths.getWorkingDir()+config.chemistry.getChemistryInput(),
					reactorDir+config.chemistry.getChemistryInput());
			//reactor setup:
			Tools.copyFile(config.paths.getWorkingDir()+reactorSetup,reactorDir+reactorSetup);
			//chemkindataDTD:
			Tools.copyFile(config.paths.getWorkingDir()+ChemkinConstants.CHEMKINDATADTD,reactorDir+ChemkinConstants.CHEMKINDATADTD);

			//Input Folder with user-defined ROP:
			for(File filename: config.paths.UDROPDir.listFiles()){//copy all files in this folder to reactor dir
				Tools.copyFile(config.paths.UDROPDir.getAbsolutePath()+"/"+filename,
						reactorDir+filename);
			}
			//instantiation of parent chemkin routine:
			AbstractChemkinRoutine routine = new ChemkinRoutine(config, runtime);
			routine.reactorOut = reactorOut;
			routine.reactorSetup = reactorSetup;
			
			//read reactor type, to be found in reactor setup file:
			BufferedReader in = new BufferedReader(new FileReader(new File(reactorDir,reactorSetup)));
			reactorType.type = reactorType.readReactorType(in);

			//PFR
			if(reactorType.type.equals(ReactorType.PLUG)){
				routine = new PFRDecorator(routine);//decoration of parent chemkin routine:
				routine.executeCKRoutine();//execution
			}

			//burner stabilized laminar premixed flame
			else if(reactorType.type.equals(ReactorType.BURNER_STABILIZED_LAMINAR_PREMIXED_FLAME)){
				routine = new PremixedFlameDecorator(routine);//decoration of parent chemkin routine:
				routine.executeCKRoutine();//execution
			}

			//CSTR
			else if (reactorType.type.equals(ReactorType.CSTR)){
				routine = new CSTRDecorator(routine);//decoration of parent chemkin routine:
				routine.executeCKRoutine();//execution
				
			}

			//ignition delay, batch reactor, transient solver, as in shock tube experiments
			else if (reactorType.type.equals(ReactorType.BATCH_REACTOR_TRANSIENT_SOLVER)){
				routine = new BatchDecorator(routine);//decoration of parent chemkin routine:
				routine.executeCKRoutine();//execution
				
			}
			//freely propagating laminar flame (flame speed experiments):
			else if(reactorType.type.equals(ReactorType.FREELY_PROPAGATING_LAMINAR_FLAME)	){
				routine = new LaminarFlameDecorator(routine);//decoration of parent chemkin routine:
				routine.executeCKRoutine();//execution
			}

			//copy reactor diagnostics file to workingdir:
			Tools.copyFile(reactorDir+reactorOut,config.paths.getWorkingDir()+reactorOut);

			//boolean first: if first time: create and adapt CKSolnList.txt file
	
			
			Tools.copyFile(reactorDir+ChemkinConstants.CKSOLNLIST,config.paths.getWorkingDir()+ChemkinConstants.CKSOLNLIST);
			
			routine = new GetSolutionDecorator(routine);//decoration of parent chemkin routine:
			routine.executeCKRoutine();//execution
			
			Tools.deleteFiles(reactorDir, ".zip");
			// if flag_excel = false: retrieve species fractions from the CKSoln.ckcsv file and continue:
			if (!flagExcel){
				BufferedReader in = new BufferedReader(new FileReader(new File(reactorDir,ChemkinConstants.CKCSVNAME)));
				if(flagIgnitionDelayExperiment){
					experiment.setValue(ModelValues.readCkcsvIgnitionDelay(in));
				}
				else if(flagFlameSpeedExperiment){
					experiment.setValue(ModelValues.readCkcsvFlameSpeed(in));
				}
				else{
					effluent.setSpeciesFractions(ModelValues.readCkcsvEffluent(in));	
				}

			}

			//if flag_excel = true: the postprocessed CKSoln.ckcsv file needs to be written to the parent directory (working directory)
			if (flagExcel){
				File excel_file = new File(reactorDir,ChemkinConstants.CKCSVNAME);
				File dummy = new File (paths.getOutputDir()+ChemkinConstants.CKCSVNAME+"_"+reactorSetup+".csv");
				excel_file.renameTo(dummy);
			}
			//delete complete reactorDir folder:
			Tools.deleteDir(new File(reactorDir));

			//when all Chemkin routines are finished, release the semaphore:
			semaphore.release();
			logger.info("license released!"+reactorSetup);

		} catch(Exception exc){
			logger.error("Exception happened in CKEmulation run() method! - here's what I know: ", exc);
			//exc.printStackTrace();
			System.exit(-1);
		}
	}
	public void preProcess(Runtime runtime) throws IOException, InterruptedException{
		//instantiation of parent chemkin routine:
		AbstractChemkinRoutine routine = new ChemkinRoutine(config, runtime);
		routine.reactorOut = reactorOut;
		routine.reactorSetup = reactorSetup;
		
		routine = new PreProcessDecorator(routine);//decoration of parent chemkin routine:
		routine.executeCKRoutine();//execution
	}
	/**
	 * checkChemInput does a preliminary check of the initial chemistry output file to verify if no errors are present.<BR>
	 * It calls the Chemkin preprocessor which produces the output file<BR>
	 * This output file is read, and the String  " NO ERRORS FOUND ON INPUT: " is sought.<BR>
	 * If this String is not present, System.exit(-1) is called<BR>
	 * @param in TODO
	 */
	public void checkChemOutput(BufferedReader in){
		try {

			/*			PrintWriter out = new PrintWriter(new FileWriter(paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT));
			chemkinRoutines.writeCKPreProcessInput(out);
			String [] preprocess = {paths.getBinDir()+"CKPreProcess",
					"-i",paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT};

			ChemkinRoutines.executeCKRoutine(preprocess, new File(paths.getWorkingDir()), runtime);
			 */
			//read the produced chem.out (path_output) file, and check if it contains error messages:
			String dummy = null;
			boolean flag = true;
			try {
				while(flag){
					dummy = in.readLine();
					if (dummy.trim().equals("NO ERRORS FOUND ON INPUT:")){
						flag = false;
					}
				}
				in.close();
				if(!flag){
					logger.info("Initial chemistry input file contains no errors. Proceed to parameter estimation!");
				}

			} catch(Exception e){
				logger.debug("Initial chemistry input file contains errors. Revision required!");
				System.exit(-1);
			}
		}catch(Exception exc){
			logger.error("exception happened - here's what I know: ", exc);
			//exc.printStackTrace();
			System.exit(-1);
		}
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

	/**
	 * @category getter
	 */
	public Map<String,Double> getEffluentValue(){
		return effluent.getSpeciesFractions();	
	}
	/**
	 * @category getter
	 */
	public Double getIgnitionValue(){
		return experiment.getValue();
	}
	/**
	 * @category getter
	 */
	public Double getFlameSpeedValue(){
		return experiment.getValue();
	}
	/**
	 * @category getter
	 * @return
	 */
	
	/**
	 * @category getter
	 * @return
	 */
	public String getReactorDir() {
		return reactorDir;
	}

	/**
	 * @category getter
	 * @return
	 */
	public Effluent getEffluent() {
		return effluent;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setEffluent(Effluent effluent) {
		this.effluent = effluent;
	}

}




