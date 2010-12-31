package parameter_estimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import parameter_estimation.levenberg.NBMTHost;

import stat.Statistics;


/**Optimization type is to be seen as the 'driver' class of the broad set of optimization algorithms available, e.g. Rosenbrock.
 * Optimization type will condition the variables of NBMT in such a way that the actual optimization routine has to be modified to
 * serve for NBMT as little as possible. It does so, for instance, by converting the 2D parameter containers (double [][]) to 
 * 1D vectors (double[]), a format that is more natural to traditional optimization routines.
 * 
 * Also, the Optimization type will serve as a server class to the specific optimization routine (e.g. Rosenbrock) to provide methods
 * that access model values getModelValues, attributes e.g. total no_parameters, maximum number of evaluations
 * 
 * @author nmvdewie
 *
 */
public class Optimization{
	static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
	private Paths paths;
	private Chemistry chemistry;
	private Experiments experiments;
	private Fitting fitting;
	private Licenses licenses;
	//1D Array of kinetic parameters
	Parameters1D params1D;
	
	public double[][] beta_new;


	boolean weightedRegression;
	private List<Map<String,Double>> exp;

	/**
	 * Rosenbrock serves as server class to Optimization.
	 * it will execute the actual optimization
	 */
	private Rosenbrock rosenbrock;

/**
 * TODO name NBMTHost is not chosen very well... modify it! 
 */
	//private NBMTmultiDHost nbmtmultiDhost;
	private NBMTHost nbmthost;
//constructor:
	public Optimization (Paths paths, Chemistry chemistry, Experiments experiments,
			Fitting fitting, Licenses licenses){
		this.paths = paths;
		this.chemistry = chemistry;
		this.fitting = fitting;
		this.experiments = experiments;	
		this.licenses = licenses;
		beta_new = new double [chemistry.getParams().getBeta().length][chemistry.getParams().getBeta()[0].length];		
//		weighted_regression = w_r;
	}
	
	
	public double [][] optimize() throws Exception{
		params1D = new Parameters1D();
		params1D.convert2Dto1D(chemistry.getParams());
		if(fitting.getFlagRosenbrock()){
			//Rosenbrock parameters:
			double efrac = 0.5;//0.3
			double succ = 3.0;
			double fail = -0.5;
			logger.info("Start of Rosenbrock!");
			rosenbrock = new Rosenbrock(this, efrac, succ, fail);
			double [] optimizedParameters = rosenbrock.returnOptimizedParameters(); 
			beta_new = Tools.convert1Dto2D(optimizedParameters, chemistry.getParams().getBeta());
		}
		if(fitting.getFlagLM()){
			logger.info("Start of Levenberg-Marquardt!");
			nbmthost = new NBMTHost(this);
			beta_new = Tools.convert1Dto2D(buildFullParamVector(nbmthost.getParms()), chemistry.getParams().getBeta());		
		}

		// move Rosenbrock monitors:
		if(new File("SSQ_Rosenbrock.csv").exists())
			Tools.moveFile(paths.getOutputDir(), "SSQ_Rosenbrock.csv");
		if(new File("output_Rosenbrock.txt").exists())
			Tools.moveFile(paths.getOutputDir(), "output_Rosenbrock.txt");

		//move LM monitors:
		if(new File("LM.txt").exists())
			Tools.moveFile(paths.getOutputDir(), "LM.txt");
		if(new File("SSQ_LM.txt").exists())
			Tools.moveFile(paths.getOutputDir(), "SSQ_LM.txt");
		if(new File("response_vars.txt").exists())
			Tools.moveFile(paths.getOutputDir(), "response_vars.txt");
		return beta_new;
	}
	
	
	public ModelValues testNewParameters(double [] parameter_guesses, boolean flag_CKSolnList) throws Exception{
		//update_chemistry_input will insert new parameter_guesses array into chem_inp
		updateChemistryInput(parameter_guesses);
		
		CKPackager ckp = new CKPackager(paths, chemistry, experiments, licenses,
				flag_CKSolnList);
		ModelValues modelValues = ckp.getModelValues();
		return modelValues;
	}
	
	
	/**
	 * converts the List<Map<String,Double>> format to a Double[][] format which is used in the LM optimization routine
	 * @return
	 */
	public Double[][] getExpDouble(){
		Double [][] dummy = new Double[exp.size()][exp.get(0).size()];
		
		// we want to have a fixed order in which the keys are called, therefore we put the response var names in a String []
		String [] species_names = new String [exp.get(0).size()];
		int counter = 0;
		for (String s : exp.get(0).keySet()){
			species_names[counter] = s;
			counter++;
		}
		
		for (int i = 0; i < exp.size(); i++){
			for (int j = 0; j < species_names.length; j++){
				dummy[i][j] = exp.get(i).get(species_names[j]);
			}
		}
		
		return dummy;
	}
	/**	
	 * plug new parameter guesses into the chemkin chemistry input file (usually chem.inp)<BR>
	 * write new update chem.inp file <BR>
	 * return the chemistry input filename<BR>
	 * WARNING: method supposes TD inside chemistry input file!!!<BR>
	 */
	public void updateChemistryInput (double [] dummy_beta_new) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(paths.getWorkingDir()+chemistry.getChemistryInput()));
		PrintWriter out = new PrintWriter(new FileWriter(paths.getWorkingDir()+"temp.inp"));
		String dummy = in.readLine();
		
		//just copy part of chem.inp about Elements, Species, Thermo
		boolean b = true;
		while(b){
			out.println(dummy);
			dummy = in.readLine();
			if (dummy.length() <= 8){
				b = true;
			}
			else if (dummy.substring(0,9).equals("REACTIONS")){
				b = false;
			}
			else {
				b = true;
			}
		}
		out.println(dummy);
		
		int counter = 0;
		for (int i = 0; i < chemistry.getParams().getFixRxns().length; i++){
			dummy = in.readLine();
			String[] st_dummy = dummy.split("\\s");
			for (int j = 0; j < chemistry.getParams().getFixRxns()[0].length; j++){
				//put new values of kinetic parameters (at position 1, 2, 3 of st_dummy[]):
				st_dummy[j+1] = Double.toString(dummy_beta_new[counter]);
				counter++;
			}
			
			dummy = st_dummy[0];
			for (int j = 1; j < st_dummy.length; j++) {
				dummy = dummy +" "+st_dummy[j];
			}
			logger.info(dummy);
			out.println(dummy);
			
		}
		
		//just copy other reactions that are not varied, until end of file:
		dummy = in.readLine();
		while(!dummy.equals("END")){
			out.println(dummy);
			dummy = in.readLine();
		}
		
		out.println(dummy);
		
		in.close();
		out.close();
		
		File f_old = new File(paths.getWorkingDir()+chemistry.getChemistryInput());
		f_old.delete();
		File f = new File(paths.getWorkingDir()+"temp.inp");
		f.renameTo(new File(paths.getWorkingDir()+chemistry.getChemistryInput()));
		
		//chemistry input file needs to be reprocessed: new link file has to be created!!!
		Runtime r = Runtime.getRuntime();
		CKEmulation c = new CKEmulation(paths, chemistry, r);
		try {
			c.callPreProcess();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedReader inChem = new BufferedReader(new FileReader(paths.getWorkingDir()+ChemkinConstants.CHEMOUT));
		c.checkChemOutput(inChem);
		try {
			c.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * retrieve_fitted_parameters returns the parameters that are loose (containing a 1 in the dummy_fix_reactions array)
	 * @return
	 */
	public double[] retrieveFittedParameters(){
		//count the number of fitted parameters:
		int no_fitted_parameters = 0;
		for (int i = 0; i < chemistry.getParams().getFixRxns().length; i++){
			for (int j = 0; j < chemistry.getParams().getFixRxns()[0].length; j++){
				no_fitted_parameters += chemistry.getParams().getFixRxns()[i][j];
			}
		}
		
		double [] fitted_parameters = new double[no_fitted_parameters];
		int counter = 0;
		for (int i = 0; i < params1D.getBeta().length; i++){
			if (params1D.getFixRxns()[i]==1){
				fitted_parameters[counter] = params1D.getBeta()[i];
				counter++;
			}
		}
		return fitted_parameters;
	}
	/**
	 * buildFullParamVector updates the full vector of parameters "dummy_beta_old", including the parameters that were fixed, from an array d 
	 * that only contains the loose parameters
	 * @param d
	 */
	public double[] buildFullParamVector(double[] d){
		int counter = 0;
		for (int i = 0; i < params1D.getBeta().length; i++){
			if (params1D.getFixRxns()[i]==1){
				params1D.getBeta()[i] = d[counter];
				counter++;
			}
		}
		return params1D.getBeta();
	}

	
	public void calcStatistics() throws Exception{
		params1D = new Parameters1D();
		params1D.convert2Dto1D(chemistry.getParams());
		nbmthost = new NBMTHost(this);
		nbmthost.bBuildJacobian();
		Statistics s = new Statistics(this);
		PrintWriter out = new PrintWriter(new FileWriter("statistics.txt"));
		//out.println("Averages of response variables:");
		//out.println(experiments.getExperimentalValues().calcExperimentalEffluentAverage());
		//out.println();
		out.println("Variance-covariance of parameter estimations:");
		Printer.printMatrix(s.get_Var_Covar(), out);
		out.println();
		out.println("Correlation matrix of parameter estimations:");
		Printer.printMatrix(s.get_Corr(), out);
		out.println();
		out.println("t-values of individual significance of parameter estimations:");
		Printer.printArray(s.getT_values(), out);
		out.println();
		out.println("tabulated t-value for alpha = 5%");
		out.println(s.getTabulated_t_value());
		out.println();
		out.println("Confidence Intervals: [parameter][upper limit][lower limit]: ");
		Printer.printMatrix(s.getConfIntervals(), out);
		out.println();
		out.println("Number of experiments:");
		out.println(experiments.getReactorInputCollector().getTotalNoExperiments());
		out.println("Number of fitted parameters:");
		out.println(chemistry.getParams().getNoFittedParameters());
		out.println("Number of responses:");
		out.println(experiments.getResponseVariables().getNoResponses());
		out.println("ANOVA: ");//Analysis of Variance:
		out.println("SRES: ");
		out.println(s.getSRES());
		out.println("SREG: ");
		out.println(s.getSREG());
		out.println("calculated F-value: ");
		out.println(s.getFvalue());
/*		
		out.println("tabulated F-value: ");
		out.println(s.getTabulated_F_value());
*/		
		out.close();
		if(new File("statistics.txt").exists())
			Tools.moveFile(paths.getOutputDir(), "statistics.txt");
	}

	public boolean isWeightedRegression() {
		return weightedRegression;
	}

	/**
	 * @category getter
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Fitting getFitting() {
		return fitting;
	}

	/**
	 * @category setter
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void setFitting(Fitting fitting) {
		this.fitting = fitting;
	}


	
	/**
	 * ####################
	 * GETTERS AND SETTERS:
	 * ####################
	 */
	
	/**
	 * @category getter
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Experiments getExperiments() {
		return experiments;
	}

	/**
	 * @category setter
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void setExperiments(Experiments experiments) {
		this.experiments = experiments;
	}

	/**
	 * @category getter
	 * @return
	 */
	public Chemistry getChemistry() {
		return chemistry;
	}

	/**
	 * @category setter
	 * @param chemistry
	 */
	public void setChemistry(Chemistry chemistry) {
		this.chemistry = chemistry;
	}
	
	/**
	 * @category getter
	 * @return
	 */
	public List<Map<String,Double>> getExp (){
		return exp;
	}
	
	/**
	 * @category getter
	 * @return
	 */
	public Parameters1D getParams1D() {
		return params1D;
	}
	
	/**
	 * @category getter
	 * @return
	 */
	public NBMTHost getNBMTHost(){
		return nbmthost;
	}
}
