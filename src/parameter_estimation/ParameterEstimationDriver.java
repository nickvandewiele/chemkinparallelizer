package parameter_estimation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class ParameterEstimationDriver {
	public static Logger logger = Logger.getLogger(ParameterEstimationDriver.class);
	static int mode;
	static Chemistry chemistry;
	static Experiments experiments;
	static Paths paths;
	static ReactorInputCollector reactorInputCollector;
	static Fitting fitting;
	static Licenses licenses;
	static Parameters2D params;
	/**
	 * @param args
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		initializeLog();
		chemistry = new Chemistry();
		reactorInputCollector = new ReactorInputCollector();
		experiments = new Experiments();
		paths = new Paths();
		fitting = new Fitting();

		BufferedReader in  = null;
		in = readINPUT(args);

		readPaths(in);

		readLicenses(in);

		readChemistryInput(in);

		readExperimentalDBPaths(in);

		readReactorDBInfo(in);

		readNoExperiments(in);

		//set no. of regular experiments:
		reactorInputCollector.setNoRegularExperiments(reactorInputCollector.getTotalNoExperiments()-reactorInputCollector.getNoIgnitionDelayExperiments() - reactorInputCollector.getNoFlameSpeedExperiments());

		int noParams = readOptimizationFlags(in);

		readMode(in);

		readReactorInputNames(in);	

		//insert ReactorInputCollector in Experiments:
		experiments.setReactorInputCollector(reactorInputCollector);

		//OPTIMIZATION SECTION: modified Arrhenius parameters [A, n, Ea]: 1 = loose, parameter will be varied; 0 = fixed, parameter will be fixed
		in.readLine();

		int noFittedReactions = readOptimizedReactions(in, noParams);

		in.close();

		setParameterBoundaries(noFittedReactions);
		switch(mode){
		case 0:	logger.info("PARITY PLOT MODE");	
		parityPlotMode();
		break;
		case 1:	logger.info("PARAMETER OPTIMIZATION MODE");		
		parameterOptimizationMode();
		break;
		case 2: logger.info("EXCEL POSTPROCESSING MODE");
		excelPostProcessingMode();
		break;
		case 3: logger.info("STATISTICS MODE");
		statisticsMode();
		break;
		}
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this program to finish: (sec) "+timeTook);
	}

	private static void statisticsMode() throws Exception {
		Param_Est p3 = new Param_Est(paths, chemistry, experiments, fitting, licenses);
		p3.statistics();
		p3.parity();
	}

	private static void excelPostProcessingMode() throws Exception {
		Param_Est p2 = new Param_Est(paths, chemistry, experiments, licenses);
		p2.excelFiles();
	}

	private static void parameterOptimizationMode() throws Exception {
		Param_Est p1 = new Param_Est(paths, chemistry, experiments, fitting, licenses);
		p1.optimizeParameters();
		p1.statistics();
		p1.parity();
	}

	private static void parityPlotMode() throws Exception {
		Param_Est p0 = new Param_Est(paths, chemistry, experiments, fitting, licenses);
		p0.parity();
		//Function f = new Function(p0.getModelValues(),p0.getExp());
		//logger.info("SSQ is: "+f.getSRES());
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

	private static void readReactorDBInfo(BufferedReader in) throws IOException {
		//user specified reactor setup data in form of separate files(false) or one single reactor setup database (true)?
		in.readLine();
		experiments.setFlagReactorDB(Boolean.parseBoolean(in.readLine()));

		// database with reactor setup data (leave blank line if file is not required for run mode)
		in.readLine();
		experiments.setReactorSetupDB(in.readLine());

		//Reactor setup method: 1: standard, 2: new method with isothermal, isobaric reactor profiles, but variable reactor lengths (type 0 if not applicable)
		in.readLine();
		experiments.setFlagReactorSetupType(new Integer(in.readLine()));
	}

	private static void readChemistryInput(BufferedReader in)
	throws IOException {
		//Chemistry input filename:
		in.readLine();
		chemistry.setChemistryInput(in.readLine());
	}

	private static void setParameterBoundaries(int noFittedReactions) {
		double [][] betaMin = new double [noFittedReactions][Chemistry.getNoparametersperreaction()];
		double [][] betaMax = new double [noFittedReactions][Chemistry.getNoparametersperreaction()];
		for (int i = 0; i < noFittedReactions; i++) {
			for (int j = 0; j < Chemistry.getNoparametersperreaction(); j++){
				betaMin[i][j]=0;
				betaMax[i][j]=1e20;
			}
		}

		params.setBetaMin(betaMin);
		params.setBetaMax(betaMax);


		chemistry.setParams(params);
	}

	private static int readOptimizedReactions(BufferedReader in, int noParams)
	throws IOException {
		//number of reactions that will be optimized:
		in.readLine();
		String dummy = in.readLine();
		int noFittedReactions = Integer.parseInt(dummy);

		int [][] fixRxns = new int [noFittedReactions][chemistry.getNoparametersperreaction()];
		for (int i = 0; i < noFittedReactions; i++){
			//comment line with "reaction i: "
			in.readLine();
			// string of 1,0,1:
			String [] s = in.readLine().split(",");
			for (int j = 0; j < chemistry.getNoparametersperreaction(); j++){
				fixRxns[i][j] = Integer.parseInt(s[j]);
			}
		}
		params = new Parameters2D(fixRxns);

		boolean flagNoParameters = checkNoParameters(fixRxns, noParams);
		if (!flagNoParameters) {
			logger.debug("Number of parameters to be fitted provided in INPUT.txt does not equal the number of ones you specified in the optimization section in INPUT.txt!");
			System.exit(-1);
		}
		else {
			//do nothing, continue
		}
		return noFittedReactions;
	}

	private static void readReactorInputNames(BufferedReader in)
	throws IOException {
		//REACTOR INPUT FILE NAMES:
		in.readLine();

		if (experiments.isFlagReactorDB()){
			in.readLine();
		}
		else {
			if(reactorInputCollector.getNoRegularExperiments()==0){
				in.readLine();
			}
			else{
				for (int i = 0; i < reactorInputCollector.getNoRegularExperiments(); i++){
					String regularReactorInput = in.readLine();	
					LinkedList<String> inputs =reactorInputCollector.getRegularInputs();
					inputs.add(regularReactorInput);
					reactorInputCollector.setRegularInputs(inputs);
				}	
			}					
		}

		//filenames of reactor input files of experiments in which ignition delay is the response variable
		in.readLine();
		if(reactorInputCollector.getNoIgnitionDelayExperiments()==0){
			in.readLine();	
		}
		else{
			for (int i = 0; i < reactorInputCollector.getNoIgnitionDelayExperiments(); i++){
				String IgnitionDelayReactorInput = in.readLine();	
				LinkedList<String> inputs =reactorInputCollector.getIgnitionDelayInputs();
				inputs.add(IgnitionDelayReactorInput);
				reactorInputCollector.setIgnitionDelayInputs(inputs);
			}

		}	

		//filenames of reactor input files of experiments in which flame speed is the response variable (leave blank line if not required)
		in.readLine();
		if(reactorInputCollector.getNoFlameSpeedExperiments()==0){
			in.readLine();	
		}
		else{
			for (int i = 0; i < reactorInputCollector.getNoFlameSpeedExperiments(); i++){
				String FlameSpeedReactorInput = in.readLine();	
				LinkedList<String> inputs =reactorInputCollector.getFlameSpeedInputs();
				inputs.add(FlameSpeedReactorInput);
				reactorInputCollector.setFlameSpeedInputs(inputs);
			}

		}
	}

	private static void readMode(BufferedReader in) throws IOException {
		in.readLine();
		mode = Integer.parseInt(in.readLine());
	}

	private static int readOptimizationFlags(BufferedReader in)
	throws IOException {
		//number of parameters to be fitted:
		in.readLine();
		String dummy = in.readLine();
		int noParams = Integer.parseInt(dummy);

		//optimization flags:
		in.readLine();
		fitting.setFlagRosenbrock(new Boolean(in.readLine()));


		in.readLine();
		fitting.setFlagLM(new Boolean(in.readLine()));

		in.readLine();
		fitting.setMaxNoEvaluations(new Integer(in.readLine()));
		return noParams;
	}

	private static void readNoExperiments(BufferedReader in) throws IOException {
		//total number of experiments:
		in.readLine();
		reactorInputCollector.setTotalNoExperiments(new Integer(in.readLine()));

		//number of experiments in which ignition delay is the response variable
		in.readLine();
		reactorInputCollector.setNoIgnitionDelayExperiments(new Integer(in.readLine()));


		//number of experiments in which flame speed is the response variable
		in.readLine();
		reactorInputCollector.setNoFlameSpeedExperiments(new Integer(in.readLine()));
	}

	private static void readExperimentalDBPaths(BufferedReader in)
	throws IOException {
		//Experimental database:(leave blank line if file is not required for run mode)
		in.readLine();		
		experiments.setPathExperimentalDB(in.readLine());

		//Ignition Delay database:(leave blank line if file is not required for run mode)
		in.readLine();
		experiments.setPathIgnitionDB(in.readLine());

		//flame speed database:(leave blank line if file is not required for run mode)
		in.readLine();
		experiments.setPathFlameSpeedDB(in.readLine());
	}

	private static void readLicenses(BufferedReader in) throws IOException {
		//Number of chemkin licenses to be used:
		in.readLine();
		int noLicenses = Integer.parseInt(in.readLine());
		licenses = new Licenses(new Integer(noLicenses));
	}

	private static void readPaths(BufferedReader in) throws IOException {
		in.readLine();
		String workingDir = in.readLine();
		if (workingDir.charAt(workingDir.length()-1)!='/'){
			logger.debug("Pathname to working directory needs to end with forward slash '/' !");
			System.exit(-1);
		}
		else {
			paths.setWorkingDir(workingDir);
		}

		//Chemkin Directory:
		in.readLine();
		paths.setChemkinDir(in.readLine());
	}

	/**
	 * check if number of parameters to be fitted, given in INPUT.txt is equal to the number of ones given in the optimization section in INPUT.txt
	 * @param fix_reactions
	 * @param no_params
	 * @return
	 */
	public static boolean checkNoParameters(int [][] fix_reactions, int no_params){
		int counter = 0;
		for (int i = 0; i < fix_reactions.length; i++){
			for (int j = 0; j < fix_reactions[0].length; j++){
				counter+=fix_reactions[i][j];
			}			
		}
		return (counter == no_params); 		
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
