package parameter_estimation;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;
/**
 * CKEmulation is designed as a Thread type, implying that multiple CKEmulations can be initiated, allowing multithreading and possible speed-up<BR>
 * CKEmulation can call several Chemkin routines: Chem, CKReactorPlugFlow, GetSolution, GetSolnTranspose depending on the task to be executed<BR>
 * In order to cope with a limited number of Chemkin licenses, a counting Semaphore type is used to keep track of the number of licenses in use<BR>
 *  
 * @author nmvdewie
 *
 */
public class CKEmulation extends Thread{
	static Logger logger = Logger.getLogger(CKEmulation.class);
	Paths paths;
	Chemistry chemistry;

	String reactorDir;

	Runtime runtime;
	boolean flagIgnitionDelayExperiment = true;

	public Experiment experiment;

	private Effluent effluent;
	private String reactorSetup;
	private String reactorOut;
	private ReactorType reactorType = new ReactorType();
	ChemkinRoutines chemkinRoutines;

	//'first' is flag that tells you if the CKSolnList needs to be constructed or not.
	boolean first;

	//'excel' tells you if the excel file (transposed CKSoln.ckcsv) needs to be created:
	boolean flagExcel;

	//Semaphore that controls chemkin license check-in and check-outs:
	Semaphore semaphore;
	private boolean flagFlameSpeedExperiment;

	//CONSTRUCTORS:
	//constructor for checking validity of chemistry input file:
	public CKEmulation(Paths paths, Chemistry chemistry, Runtime runtime){
		this.experiment = new Experiment();
		this.effluent = new Effluent();
		this.paths = paths;
		this.chemistry = chemistry;
		this.runtime = runtime;
		this.chemkinRoutines = new ChemkinRoutines(paths.getWorkingDir(), paths.getBinDir(), chemistry.getChemistryInput());
	}

	//constructor for creating CKSolnList.txt
	public CKEmulation(Paths paths, Chemistry chemistry, Runtime runtime,
			String reactorSetup, boolean first){
		this(paths, chemistry, runtime);
		this.reactorSetup = reactorSetup;
		this.first = first;
	}

	//constructor for running 'classical' Chemkin routines
	public CKEmulation(Paths paths, Chemistry chemistry, Runtime runtime,
			String rs, Semaphore s, boolean flagCKSolnList, boolean flagExcel, 
			boolean flagIgnitionDelay, boolean flagFlameSpeed) throws Exception{
		this(paths, chemistry, runtime, rs, flagCKSolnList);
		int length = rs.length();
		this.reactorOut = rs.substring(0,(length-4))+".out";

		this.flagExcel = flagExcel;
		this.flagIgnitionDelayExperiment = flagIgnitionDelay;
		this.flagFlameSpeedExperiment = flagFlameSpeed;
		this.semaphore = s;

		this.reactorDir = paths.getWorkingDir()+"temp_ "+rs.substring(0,(length-4))+"/";
		boolean temp = new File(reactorDir).mkdir();
		if(!temp){
			logger.debug("Creation of reactor directory failed!");
			System.exit(-1);
		}


		this.chemkinRoutines = new ChemkinRoutines(paths.getWorkingDir(), paths.getBinDir(), reactorDir, chemistry.getChemistryInput());

		copyLinkFiles(paths);

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
	 * run() is the method that will be executed when Thread.start() is executed. Its argument list is void (mandatory I think).
	 */
	public void run(){
		try {
			semaphore.acquire();

			logger.info("license acquired!"+reactorSetup);

			//copy chemistry input to the reactorDir:
			Tools.copyFile(paths.getWorkingDir()+chemistry.getChemistryInput(),
					reactorDir+chemistry.getChemistryInput());
			//reactor setup:
			Tools.copyFile(paths.getWorkingDir()+reactorSetup,reactorDir+reactorSetup);
			//chemkindataDTD:
			Tools.copyFile(paths.getWorkingDir()+ChemkinConstants.CHEMKINDATADTD,reactorDir+ChemkinConstants.CHEMKINDATADTD);

			//Input Folder with user-defined ROP:
			for(String filename: paths.UDROPDir.list()){//copy all files in this folder to reactor dir
				Tools.copyFile(paths.UDROPDir.getAbsolutePath()+"/"+filename,
						reactorDir+filename);
			}
			callReactor();

			//copy reactor diagnostics file to workingdir:
			Tools.copyFile(reactorDir+reactorOut,paths.getWorkingDir()+reactorOut);

			//boolean first: if first time: create and adapt CKSolnList.txt file
			if (first){
				//if(!flagExcel){
					chemkinRoutines.createSolnList(runtime);
					BufferedReader in = new BufferedReader(new FileReader(new File(reactorDir,ChemkinConstants.CKSOLNLIST)));
					writeSolnList(in);
					//copy the newly created CKSolnList to the workingDir so that it can be picked up by the other CK_emulations:
					Tools.copyFile(reactorDir+ChemkinConstants.CKSOLNLIST,paths.getWorkingDir()+ChemkinConstants.CKSOLNLIST);					
				//}

			}
			else {
				//copy the CKSolnList to the reactorDir
				//if(!flagExcel){
					Tools.copyFile(paths.getWorkingDir()+ChemkinConstants.CKSOLNLIST,reactorDir+ChemkinConstants.CKSOLNLIST);	
				//}
				
			}

			chemkinRoutines.callGetSol(runtime);

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
	/**
	 * call a Chemkin reactor model (ic: CKReactorPlugFlow) executable	
	 * @throws Exception
	 */
	public void callReactor () throws IOException, InterruptedException {
		//read reactor type, to be found in reactor setup file:
		BufferedReader in = new BufferedReader(new FileReader(new File(reactorDir,reactorSetup)));
		reactorType.type = reactorType.readReactorType(in);
		//PFR
		if(reactorType.type.equals(ReactorType.PLUG)){
			chemkinRoutines.callPFR(reactorSetup, reactorOut, runtime);	
		}

		//burner stabilized laminar premixed flame
		else if(reactorType.type.equals(ReactorType.BURNER_STABILIZED_LAMINAR_PREMIXED_FLAME)){
			chemkinRoutines.callBurnerStabilizedFlame(reactorSetup, reactorOut, runtime);
		}

		//CSTR
		else if (reactorType.type.equals(ReactorType.CSTR)){
			chemkinRoutines.callGenericPSR(reactorSetup, reactorOut, runtime);
		}

		//ignition delay, batch reactor, transient solver, as in shock tube experiments
		else if (reactorType.type.equals(ReactorType.BATCH_REACTOR_TRANSIENT_SOLVER)){
			chemkinRoutines.callGenericClosed(reactorSetup, reactorOut, runtime);
		}
		//freely propagating laminar flame (flame speed experiments):
		else if(reactorType.type.equals(ReactorType.FREELY_PROPAGATING_LAMINAR_FLAME)	){
			chemkinRoutines.callFreelyPropagatingFlame(reactorSetup,reactorOut,runtime);

		}

	}
	public void preProcess(Runtime runtime){
		try {
			chemkinRoutines.callPreProcess(runtime);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
	 * Set the SolnList.txt to the desired format:<BR>
	 * <LI>lines with # are left untouched</LI>
	 * <LI>no information whatsoever for all variables, except species, MW, exit_mass_flow_rate</LI>
	 * <LI>no sensitivity info for species, MW, exit_mass_flow_rate</LI>
	 * <LI>set UNIT of distance to (cm)</LI>
	 * <LI>all species mole fractions are reported, also those with negative fractions: FILTER MIN</LI>
	 * @param in TODO
	 * @throws Exception
	 * TODO when dealing with shock tube experiments during which ignition delays are measured, the response variable
	 * is ignition delay and not a species mass fraction.
	 */
	public void writeSolnList(BufferedReader in)throws Exception{
		String temp = "tempList.txt";
		PrintWriter out = new PrintWriter(new FileWriter(new File(reactorDir,temp)));
		try{
			File speciesFile = new File(paths.getWorkingDir(),ChemkinConstants.CHEMASU);
			BufferedReader inSpecies = new BufferedReader (new FileReader(speciesFile));
			LinkedList<String> speciesNames = Chemistry.readSpeciesNames(inSpecies);
			String dummy = null;
			dummy = in.readLine();

			//if a comment line (starts with char '#') is read, just copy it to output file
			while(in.ready()){
				//if a blank line is read, just copy and continue
				if (dummy.trim().length()==0){
					out.println(dummy);
					dummy = in.readLine();
					//System.out.println(dummy);
				}
				//if a comment line (#) is read, just copy and continue
				else if (dummy.charAt(0)=='#'||(dummy.trim().length()==0)) {
					out.println(dummy);
					dummy = in.readLine();
					//System.out.println(dummy);
				}
				else {
					//separator are TWO spaces, not just one space!
					String[] st_dummy = dummy.split("  ");
					//only species variables and molecular weight variable are reported:
					if (st_dummy[0].trim().equals("VARIABLE")){
						//check if the 2nd keyword matches "molecular weight":
						if (st_dummy[1].trim().equals("molecular_weight")){							
							//no sensitivity info for molecular weight variable
							st_dummy[4]="0";
						}
						//check if the 2nd keyword matches "exit_mass_flow_rate":
						else if(st_dummy[1].trim().equals("exit_mass_flow_rate")){
							st_dummy[4]="0";
						}
						//check if 2nd keyword is one of the species names:
						else if(speciesNames.contains(st_dummy[1])){
							//no sensitivity info for species: set last number in the line to zero
							st_dummy[4]="0";
						}
						//ignition data should also considered:
						else if(st_dummy[1].trim().equals("ignition_data")){
							//check whether this experiments is about ignition delays:
							if(flagIgnitionDelayExperiment){
								//no sensitivity
								st_dummy[4]="0";
							}
							//do not report ignition delay data, even
							else {
								st_dummy[2]="0";
								st_dummy[4]="0";
							}
						}
						else if(st_dummy[1].trim().equals("flame_speed")){
							//check whether this experiments is about flame speeds:
							if(flagFlameSpeedExperiment){
								//no sensitivity
								st_dummy[4]="0";
							}
							//do not report flame speed data, even
							else {
								st_dummy[2]="0";
								st_dummy[4]="0";
							}
						}

						//the rest of the variables are set to zero and will not be reported in the .ckcsv file
						else {

							//st_dummy[3] is standard equal to zero
							st_dummy[2]="0";
							st_dummy[4]="0";

						}
					}
					//set UNIT of Distance to m instead of cm:
					else if(st_dummy[0].trim().equals("UNIT")){
						if (st_dummy[1].trim().equals("Distance")){
							st_dummy[2]="(cm)";
						}
					}

					//make sure even negative mole fractions are printed:
					else if(st_dummy[0].trim().equals("FILTER")){
						if (st_dummy[1].trim().equals("MIN")){
							st_dummy[2]="-1.0";
						}
					}

					//concatenate String array back to its original form:
					String dummy_out = st_dummy[0];
					//add double spaces between Strings again:
					for(int i=1;i<st_dummy.length;i++){
						dummy_out += "  "+st_dummy[i];
					}

					out.println(dummy_out);
					dummy = in.readLine();
					//System.out.println(dummy);
				}
			} 
		}catch (Exception e){//do nothing: e catches the end of the file exception

		}
		in.close();
		out.close();
		//remove old CKSolnList and replace it with newly create one
		File old = new File(reactorDir,ChemkinConstants.CKSOLNLIST);
		old.delete();
		File f_temp = new File(reactorDir,temp);
		f_temp.renameTo(new File(reactorDir,ChemkinConstants.CKSOLNLIST));
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
	public Paths getPaths() {
		return paths;
	}
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




