package parameter_estimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
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
	Paths paths;
	Parameters2D params2D;
	Parameters1D params1D;
	public Parameters1D getParams1D() {
		return params1D;
	}
	public int maxeval;
	public double[][] beta_new;


	boolean flagRosenbrock;
	boolean flagLM;
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
	public Optimization (Paths paths, Parameters2D params, int m, boolean f_r, boolean f_L, List<Map<String,Double>>exp){
		this.paths = paths;
		this.params2D = params;
		maxeval = m;
		beta_new = new double [params.getBeta().length][params.getBeta()[0].length];

		
		flagRosenbrock = f_r;
		flagLM = f_L;
//		weighted_regression = w_r;
		
		this.exp = exp;
	}
	
	
	public double [][] optimize(List<Map<String,Double>> exp) throws IOException, InterruptedException{
		Set<String> response_vars = exp.get(0).keySet();
		PrintWriter out_species = new PrintWriter(new FileWriter("response_vars.txt"));
		for(Iterator<String> it = response_vars.iterator(); it.hasNext();){
			out_species.println((String)it.next());
		}
		out_species.close();
		params1D = new Parameters1D();
		params1D.convert2Dto1D(params2D);
		if(flagRosenbrock){
			//Rosenbrock parameters:
			double efrac = 0.5;//0.3
			double succ = 3.0;
			double fail = -0.5;
			logger.info("Start of Rosenbrock!");
			rosenbrock = new Rosenbrock(this, efrac, succ, fail, maxeval);
			beta_new = Tools.convert1Dto2D(rosenbrock.returnOptimizedParameters(), params2D.getBeta());
		}
		
		if(flagLM){
			logger.info("Start of Levenberg-Marquardt!");
			nbmthost = new NBMTHost(this);
			beta_new = Tools.convert1Dto2D(buildFullParamVector(nbmthost.getParms()), params2D.getBeta());		
		}


		// Rosenbrock monitors:
		if(new File("SSQ_Rosenbrock.csv").exists())
			Tools.moveFile(paths.getOutputDir(), "SSQ_Rosenbrock.csv");
		if(new File("output_Rosenbrock.txt").exists())
			Tools.moveFile(paths.getOutputDir(), "output_Rosenbrock.txt");

		//LM monitors:
		if(new File("LM.txt").exists())
			Tools.moveFile(paths.getOutputDir(), "LM.txt");
		if(new File("SSQ_LM.txt").exists())
			Tools.moveFile(paths.getOutputDir(), "SSQ_LM.txt");
		if(new File("response_vars.txt").exists())
			Tools.moveFile(paths.getOutputDir(), "response_vars.txt");

		return beta_new;
	}
	
	
	public List<Map<String,Double>> getModelValues(double [] parameter_guesses, boolean flag_CKSolnList) throws IOException, InterruptedException{
		//update_chemistry_input will insert new parameter_guesses array into chem_inp
		update_chemistry_input(parameter_guesses);
		
		List<Map<String,Double>> model = new ArrayList<Map<String,Double>>();
		CKPackager ckp_new = new CKPackager(paths, flag_CKSolnList);
		model = ckp_new.getModelValues();
		return model;
	}
	
	public List<Map<String,Double>> getExp (){
		return exp;
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
	public void update_chemistry_input (double [] dummy_beta_new) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(paths.getWorkingDir()+paths.getChemInp()));
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
		for (int i = 0; i < params2D.getFixRxns().length; i++){
			dummy = in.readLine();
			String[] st_dummy = dummy.split("\\s");
			for (int j = 0; j < params2D.getFixRxns()[0].length; j++){
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
		
		File f_old = new File(paths.getWorkingDir()+paths.getChemInp());
		f_old.delete();
		File f = new File(paths.getWorkingDir()+"temp.inp");
		f.renameTo(new File(paths.getWorkingDir()+paths.getChemInp()));
	}
	
	/**
	 * retrieve_fitted_parameters returns the parameters that are loose (containing a 1 in the dummy_fix_reactions array)
	 * @return
	 */
	public double[] retrieve_fitted_parameters(){
		//count the number of fitted parameters:
		int no_fitted_parameters = 0;
		for (int i = 0; i < params2D.getFixRxns().length; i++){
			for (int j = 0; j < params2D.getFixRxns()[0].length; j++){
				no_fitted_parameters += params2D.getFixRxns()[i][j];
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

	public NBMTHost getNBMTHost(){
		return nbmthost;
	}
	public void calcStatistics() throws IOException, InterruptedException{
		params1D = new Parameters1D();
		params1D.convert2Dto1D(params2D);
		nbmthost = new NBMTHost(this, true);
		nbmthost.bBuildJacobian();
		//beta_new = convert_1D_to_2D(buildFullParamVector(nbmthost.getParms()));
		Statistics s = new Statistics(this);
		PrintWriter out = new PrintWriter(new FileWriter("statistics.txt"));
		out.println("Averages of response variables:");
		//out.println(this.nbmtmultiDhost.getFunction().calcAverage());
		out.println(this.nbmthost.getFunction().calcAverage());
		out.println();
		out.println("Variance-covariance of parameter estimations:");
		s.printMatrix(s.get_Var_Covar(), out);
		out.println();
		out.println("Correlation matrix of parameter estimations:");
		s.printMatrix(s.get_Corr(), out);
		out.println();
		out.println("t-values of individual significance of parameter estimations:");
		s.printArray(s.getT_values(), out);
		out.println();
		out.println("tabulated t-value for alpha = 5%");
		out.println(s.getTabulated_t_value());
		out.println();
		out.println("Confidence Intervals: [parameter][upper limit][lower limit]: ");
		s.printMatrix(s.getConfidence_intervals(), out);
		out.println();
		out.println("Number of experiments:");
		out.println(s.getNo_experiments());
		out.println("Number of parameters:");
		out.println(s.getNo_parameters());
		out.println("Number of responses:");
		out.println(s.getNo_responses());
		out.println("ANOVA: ");//Analysis of Variance:
		out.println("SRES: ");
		out.println(s.getSRES());
		out.println("SREG: ");
		out.println(s.getSREG());
		out.println("calculated F-value: ");
		out.println(s.getF_value());
/*		
		out.println("tabulated F-value: ");
		out.println(s.getTabulated_F_value());
*/		
		out.close();
		if(new File("statistics.txt").exists())
			Tools.moveFile(paths.getOutputDir(), "statistics.txt");
	}

	public boolean isWeighted_regression() {
		return weightedRegression;
	}

}
