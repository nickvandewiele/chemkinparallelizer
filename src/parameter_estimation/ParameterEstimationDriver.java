package parameter_estimation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
	//this comment is added to verify version control system
	/**
	 * @param args
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		initializeLog();
		Chemistry chemistry = new Chemistry();
		Experiments experiments = new Experiments();
		Paths paths = new Paths();
		Fitting fitting = new Fitting();

		//input file will be searched in working directory under the name INPUT.txt:
		BufferedReader in = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/INPUT.txt"));


		in.readLine();
		String workingDir = in.readLine();
		if (workingDir.charAt(workingDir.length()-1)!='/'){
			logger.debug("Pathname to working directory needs to end with forward slash '/' !");
			System.exit(-1);
		}
		else {
			paths.setWorkingDir(workingDir);
		}

		in.readLine();
		paths.setChemkinDir(in.readLine());

		in.readLine();
		int noLicenses = Integer.parseInt(in.readLine());
		Licenses licenses = new Licenses(new Integer(noLicenses));

		in.readLine();
		chemistry.setChemistryInput(in.readLine());

		in.readLine();		
		experiments.setPathExperimentalDB(in.readLine());

		in.readLine();
		experiments.setPathIgnitionDB(in.readLine());

		in.readLine();
		experiments.setFlagReactorDB(Boolean.parseBoolean(in.readLine()));

		in.readLine();
		experiments.setReactorSetupDB(in.readLine());

		in.readLine();
		experiments.setFlagReactorSetupType(new Integer(in.readLine()));

		//total number of experiments:
		in.readLine();
		experiments.setTotalNoExperiments(new Integer(in.readLine()));

		//number of experiments in which ignition delay is the response variable
		in.readLine();
		experiments.setNoIgnitionDelayExperiments(new Integer(in.readLine()));
		experiments.setNoRegularExperiments(experiments.getTotalNoExperiments()-experiments.getNoIgnitionDelayExperiments());

		//number of parameters to be fitted:
		in.readLine();
		int noParams = Integer.parseInt(in.readLine());

		//optimization flags:
		in.readLine();
		fitting.setFlagRosenbrock(new Boolean(in.readLine()));


		in.readLine();
		fitting.setFlagLM(new Boolean(in.readLine()));

		in.readLine();
		fitting.setMaxNoEvaluations(new Integer(in.readLine()));

		in.readLine();
		mode = Integer.parseInt(in.readLine());

		//REACTOR INPUT FILE NAMES:
		in.readLine();

		if (experiments.isFlagReactorDB()){
			in.readLine();
		}
		else {
			if(experiments.getNoRegularExperiments()==0){
				in.readLine();
			}
			else{
				for (int i = 0; i < experiments.getNoRegularExperiments(); i++){
					String regularReactorInput = in.readLine();	
					LinkedList<String> inputs =experiments.getRegularReactorInputs();
					inputs.add(regularReactorInput);
					experiments.setRegularReactorInputs(inputs);
				}	
			}					
		}

		//filenames of reactor input files of experiments in which ignition delay is the response variable
		in.readLine();
		if(experiments.getNoIgnitionDelayExperiments()==0){
			in.readLine();	
		}
		else{
			for (int i = 0; i < experiments.getNoIgnitionDelayExperiments(); i++){
				String IgnitionDelayReactorInput = in.readLine();	
				LinkedList<String> inputs =experiments.getIgnitionDelayInputs();
				inputs.add(IgnitionDelayReactorInput);
				experiments.setIgnitionDelayInputs(inputs);
			}

		}				


		//OPTIMIZATION SECTION: modified Arrhenius parameters [A, n, Ea]: 1 = loose, parameter will be varied; 0 = fixed, parameter will be fixed
		in.readLine();

		//number of reactions that will be optimized:
		in.readLine();
		int noFittedReactions = Integer.parseInt(in.readLine());

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
		Parameters2D params = new Parameters2D(fixRxns);

		boolean flagNoParameters = checkNoParameters(fixRxns, noParams);
		if (!flagNoParameters) {
			logger.debug("Number of parameters to be fitted provided in INPUT.txt does not equal the number of ones you specified in the optimization section in INPUT.txt!");
			System.exit(-1);
		}
		else {
			//do nothing, continue
		}

		in.close();


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

		switch(mode){
		case 0:	logger.info("PARITY PLOT MODE");	
		Param_Est p0 = new Param_Est(paths, chemistry, experiments, fitting, licenses);
		p0.parity();
		//Function f = new Function(p0.getModelValues(),p0.getExp());
		//logger.info("SSQ is: "+f.getSRES());
		break;
		case 1:	logger.info("PARAMETER OPTIMIZATION MODE");		
		Param_Est p1 = new Param_Est(paths, chemistry, experiments, fitting, licenses);
		p1.optimizeParameters();
		p1.statistics();
		p1.parity();
		break;
		case 2: logger.info("EXCEL POSTPROCESSING MODE");
		Param_Est p2 = new Param_Est(paths, chemistry, experiments, licenses);
		p2.excelFiles();
		break;
		case 3: logger.info("STATISTICS MODE");
		Param_Est p3 = new Param_Est(paths, chemistry, experiments, fitting, licenses);
		p3.statistics();
		p3.parity();
		}
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this program to finish: (sec) "+timeTook);
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
