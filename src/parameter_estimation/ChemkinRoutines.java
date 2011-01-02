package parameter_estimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


/**
 * location for all pointers to chemkin routines, and the required arguments
 * syntax
 * @author nmvdewie
 *
 */
public class ChemkinRoutines extends Loggable{
	String workingDir;
	String binDir;
	String reactorDir;
	String chemistryInput;
	
	//Runtime runtime;
	
	public ChemkinRoutines(String workingDir, String binDir, String reactorDir, String chemistryInput){
		this.workingDir = workingDir;
		this.binDir = binDir;
		this.reactorDir = reactorDir;
		this.chemistryInput = chemistryInput;
	}
	public ChemkinRoutines(String workingDir, String binDir, String chemistryInput){
		this.workingDir = workingDir;
		this.binDir = binDir;
		this.chemistryInput = chemistryInput;
	}
	/**
	 * this routine overloads the standard execute_CKRoutine with a specified working directory, different from the standard working directory
	 * @param CKCommand
	 * @param directory
	 * @param runtime TODO
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void executeCKRoutine(String [] CKCommand, File directory, Runtime runtime) throws IOException, InterruptedException{
		String s = null;
		String [] environment = null;
		Process p = runtime.exec(CKCommand, environment, directory);
	
		BufferedReader stdInput_p = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader stdError_p = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	
		// read the output from the command
		CKEmulation.logger.info("Here is the standard output of the command:\n");
		while ((s = stdInput_p.readLine()) != null) {
			CKEmulation.logger.info(s);
		}
		stdInput_p.close();
		// read any errors from the attempted command
		CKEmulation.logger.debug("Here is the standard error of the command (if any):\n");
		while ((s = stdError_p.readLine()) != null) {
			CKEmulation.logger.debug(s);
		}
		stdError_p.close();
	
		p.waitFor();
		p.destroy();
		//System.out.println("Setup finished");		
	}

	public static void executeCKRoutine (String [] CKCommand, Runtime runtime) throws IOException, InterruptedException{
		String s = null;	
		Process p = runtime.exec(CKCommand);
	
		BufferedReader stdInput_p = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader stdError_p = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	
		// read the output from the command
		CKEmulation.logger.info("Here is the standard output of the command:\n");
		while ((s = stdInput_p.readLine()) != null) {
			CKEmulation.logger.info(s);
		}
		stdInput_p.close();
		// read any errors from the attempted command
		CKEmulation.logger.debug("Here is the standard error of the command (if any):\n");
		while ((s = stdError_p.readLine()) != null) {
			CKEmulation.logger.debug(s);
		}
		stdError_p.close();
	
		p.waitFor();
		p.destroy();
		//System.out.println("Setup finished");		
	}
	/**
	 * calls the Chemkin CKSolnTranspose executable
	 * @param runtime TODO
	 * @throws Exception
	 */
	public void callTranspose (Runtime runtime) throws Exception {
		String [] progTranspose = {binDir+"CKSolnTranspose",
				reactorDir+ChemkinConstants.CKCSVNAME};
		executeCKRoutine(progTranspose, runtime);
	
	}
	/**
	 * call the Chemkin preprocessor chem and produces the linking file (.asc)
	 * @param runtime TODO
	 * @throws Exception
	 */
	public void callChem (Runtime runtime) throws IOException, InterruptedException {
		String [] preprocess = {binDir+"chem","-i",
				reactorDir+chemistryInput,"-o",
				reactorDir+ChemkinConstants.CHEMOUT,"-c",
				reactorDir+ChemkinConstants.CHEMASC};
		ChemkinRoutines.executeCKRoutine(preprocess, runtime);
	}
	/**
	 * calls the .bat file that sets environment variables for proper use of future Chemkin calls<BR>
	 * @param runtime TODO
	 * @throws Exception
	 */
	public void callBat (Runtime runtime) throws IOException, InterruptedException {
		String [] setup_environment = {binDir+"run_chemkin_env_setup.bat"};
		ChemkinRoutines.executeCKRoutine(setup_environment, runtime);
	}
	/**
	 * processes the xmldata.zip file using Chemkin GetSolution executable
	 * it produces a .ckcsv file with only the lines left that were specified in the CKSolnList.txt
	 * @param runtime TODO
	 * @param flag_massfrac if true: GetSolution prints mass fractions instead of mole fractions (used for Excel Postprocessing and Parity mode)
	 * @throws Exception
	 */
	public  void callGetSol (Runtime runtime) throws IOException, InterruptedException {
		//String abbrev_path = cd+"data/abbreviations.csv";
		/**
		 * nosen: no sensitivity data is included
		 * norop: no rate of production info is included
		 */
		String [] progGetSol = {binDir+"GetSolution",
				"-nosen","-norop","-mass",
				reactorDir+ChemkinConstants.XML};
		ChemkinRoutines.executeCKRoutine(progGetSol, new File (reactorDir), runtime);
	
		Tools.deleteFiles(reactorDir, ".zip");
	}
	/**
	 * createSolnList creates the CKSolnList.txt file by calling the "GetSolution -listonly" routine<BR>
	 * CKSolnList contains
	 * @param runtime TODO
	 * @throws Exception
	 */
	public void createSolnList(Runtime runtime)throws Exception{
		String [] progGetList = {binDir+"GetSolution","-listonly",
				reactorDir+ChemkinConstants.XML};
		ChemkinRoutines.executeCKRoutine(progGetList, new File(reactorDir), runtime);	
	}
	/**
	 * create CKPreprocess.input file with directions to chem_inp and output/link files of chemistry and transport:
	 * @param out TODO
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void writeCKPreProcessInput(PrintWriter out)throws IOException, InterruptedException{
		//in windows: user.dir needs to be followed by "\", in *nix by "/"... 
		String osname = System.getProperty("os.name");
		char osChar;
		if (osname.equals("Linux")){
			osChar = '/';
		}
		else {
			osChar = '\\';
		}		
			out.println("IN_CHEM_INPUT="+workingDir+osChar+chemistryInput);
	
			//chemistry output, link and species list:
			out.println("OUT_CHEM_OUTPUT="+workingDir+osChar+ChemkinConstants.CHEMOUT);
			out.println("OUT_CHEM_ASC="+workingDir+osChar+ChemkinConstants.CHEMASC);
			out.println("OUT_CHEM_SPECIES="+workingDir+osChar+ChemkinConstants.CHEMASU);
	
			//transport link files and log file:
			out.println("FIT_TRANSPORT_PROPERTIES=1");
			out.println("OUT_TRAN_OUTPUT="+workingDir+osChar+ChemkinConstants.TRANOUT);
			out.println("OUT_TRAN_ASC="+workingDir+osChar+ChemkinConstants.TRANASC);
	
			out.close();
	}
	public void callGenericClosed(String reactorSetup, String reactorOut, Runtime runtime) throws IOException, InterruptedException {
		String [] input = {binDir+"CKReactorGenericClosed","-i",
				reactorDir+reactorSetup,"-o",
				reactorDir+reactorOut};
		ChemkinRoutines.executeCKRoutine(input, new File(reactorDir),
				runtime);
	}
	public void callBurnerStabilizedFlame(String reactorSetup, String reactorOut, Runtime runtime) throws IOException,
			InterruptedException {
		String [] input = {binDir+"CKReactorBurnerStabilizedFlame",
				"-i",reactorDir+reactorSetup,"-o",
				reactorDir+reactorOut};
		ChemkinRoutines.executeCKRoutine(input, new File(reactorDir), runtime);
	}
	public void callPFR(String reactorSetup, String reactorOut, Runtime runtime) throws IOException, InterruptedException {
		String [] input = {binDir+"CKReactorPlugFlow","-i",
				reactorDir+reactorSetup,"-o",
				reactorDir+reactorOut};
		ChemkinRoutines.executeCKRoutine(input, new File(reactorDir), runtime);
	}
	public void callGenericPSR(String reactorSetup, String reactorOut, Runtime runtime) throws IOException, InterruptedException {
		String [] input = {binDir+"CKReactorGenericPSR","-i",
				reactorDir+reactorSetup,"-o",
				reactorDir+reactorOut};
		ChemkinRoutines.executeCKRoutine(input, new File(reactorDir), runtime);
	}
	/**
	 * The Chemkin routine CKPreProcess requires an input file that contains:
	 *  Species info, TD data, Transport data (if present), Mechanism data
	 * @param runtime TODO
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void callPreProcess(Runtime runtime) throws IOException, InterruptedException {
		System.out.println(workingDir+ChemkinConstants.PREPROCESSINPUT);
		PrintWriter out = new PrintWriter(new FileWriter(workingDir+ChemkinConstants.PREPROCESSINPUT));
		writeCKPreProcessInput(out);
		
		
		String [] preprocess = {binDir+"CKPreProcess",
				"-i",workingDir+ChemkinConstants.PREPROCESSINPUT};
		
		ChemkinRoutines.executeCKRoutine(preprocess, new File(workingDir), runtime);
	}
	public void callFreelyPropagatingFlame(String reactorSetup,
			String reactorOut, Runtime runtime) throws IOException, InterruptedException {
		String [] input = {binDir+"CKReactorFreelyPropagatingFlame","-i",
				reactorDir+reactorSetup,"-o",
				reactorDir+reactorOut}; 
		ChemkinRoutines.executeCKRoutine(input, new File(reactorDir), runtime);
	}
	
}
