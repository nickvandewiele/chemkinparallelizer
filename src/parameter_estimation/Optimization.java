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

import stat.Statistics;

import levenberg.mono.NBMTHost;

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
public class Optimization extends Paths{
	public int maxeval;
	public double[][] beta_old;
	public double[][] beta_new;
	public double[][] beta_min;
	public double[][] beta_max;
	public int[][] fix_reactions;
	

	private double[] dummy_beta;

	private double[] dummy_beta_min;
	private double[] dummy_beta_max;
	private int[] dummy_fix_reactions;
	
	private int totalNoParameters;
	
	public int getTotal_no_parameters() {
		return totalNoParameters;
	}
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
	public Optimization (String wd, String cd, int m, double [][] b_old, String[] r_inp, int no_lic, String c_inp, double [][] b_min, double [][] b_max, int [][] f_rxns, boolean f_r, boolean f_L, List<Map<String,Double>>exp){
		super(wd, cd, c_inp, r_inp, no_lic);
		maxeval = m;
		beta_old = b_old;
		beta_new = new double [b_old.length][b_old[0].length];
		beta_min = b_min;
		beta_max = b_max;
		fix_reactions = f_rxns;
		
		flagRosenbrock = f_r;
		flagLM = f_L;
//		weighted_regression = w_r;
		
		this.exp = exp;
	}
	
	/**
	 * convert2D_to_1D() will initialize the 1D vectors derived from the 2D matrices containing parameter data to be used in optimization routine
	 */
	public void convert2Dto1D(){
		//for programming ease of the optimization algorithm, the double [][] matrix b_old (containing parameters per reaction)
		//will be converted to a 1D vector:	
		totalNoParameters = fix_reactions.length * fix_reactions[0].length;
	
		dummy_beta = new double[totalNoParameters];

		dummy_beta_min = new double[totalNoParameters];
		dummy_beta_max = new double[totalNoParameters];
		dummy_fix_reactions = new int[totalNoParameters];
			
		//copy values of [][] matrices to [] vectors:
		int counter = 0;
		for (int i = 0; i < fix_reactions.length; i++) {
			for (int j = 0; j < fix_reactions[0].length; j++){
				dummy_beta[counter] = beta_old[i][j];
				dummy_beta_min[counter] = beta_min[i][j];
				dummy_beta_max[counter] = beta_max[i][j];
				dummy_fix_reactions[counter] = fix_reactions[i][j];				
				counter++;
			}
		}
	}	
	public double [][] optimize(List<Map<String,Double>> exp) throws Exception{
		Set<String> response_vars = exp.get(0).keySet();
		PrintWriter out_species = new PrintWriter(new FileWriter("response_vars.txt"));
		for(Iterator<String> it = response_vars.iterator(); it.hasNext();){
			out_species.println((String)it.next());
		}
		out_species.close();
		convert2Dto1D();
		if(flagRosenbrock){
			//Rosenbrock parameters:
			double efrac = 0.5;//0.3
			double succ = 3.0;
			double fail = -0.5;
			System.out.println("Start of Rosenbrock!");
			rosenbrock = new Rosenbrock(this, efrac, succ, fail, maxeval);
			beta_new = convert1Dto2D(rosenbrock.returnOptimizedParameters());
		}
		
		if(flagLM){
			System.out.println("Start of Levenberg-Marquardt!");
			nbmthost = new NBMTHost(this);
			beta_new = convert1Dto2D(buildFullParamVector(nbmthost.getParms()));		
		}


		// Rosenbrock monitors:
		if(new File("SSQ_Rosenbrock.csv").exists())
			moveFile(outputDir, "SSQ_Rosenbrock.csv");
		if(new File("output_Rosenbrock.txt").exists())
			moveFile(outputDir, "output_Rosenbrock.txt");

		//LM monitors:
		if(new File("LM.txt").exists())
			moveFile(outputDir, "LM.txt");
		if(new File("SSQ_LM.txt").exists())
			moveFile(outputDir, "SSQ_LM.txt");
		if(new File("response_vars.txt").exists())
			moveFile(outputDir, "response_vars.txt");

		return beta_new;
	}
	
	/**
	 * convert 1D vector into 2D matrix: 
	 * @return
	 */
	public double [][] convert1Dto2D(double [] vector){
		//convert 1D vector back to matrix [][] notation:
		double [][] matrix = new double [beta_old.length][beta_old[0].length];
		int counter = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++){
				matrix[i][j] = vector[counter];
				counter++;
			}
		}
		return matrix;
	}

	//getters for 1D vectors:
	public double [] getDummyBetaOld(){
		return dummy_beta;
	}

	public double [] getDummyBetaMin(){
		return dummy_beta_min;
	}
	public double [] get_dummy_beta_max(){
		return dummy_beta_max;
	}
	public int [] get_dummy_fix_reactions(){
		return dummy_fix_reactions;
	}
	
	public int get_total_no_parameters(){
		return totalNoParameters;
	}
	
	public List<Map<String,Double>> getModelValues(double [] parameter_guesses, boolean flag_CKSolnList) throws Exception{
		//update_chemistry_input will insert new parameter_guesses array into chem_inp
		update_chemistry_input(parameter_guesses);
		
		List<Map<String,Double>> model = new ArrayList<Map<String,Double>>();
		CKPackager ckp_new = new CKPackager(workingDir, chemkinDir, chem_inp, reactorInputs, noLicenses, flag_CKSolnList);
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
		BufferedReader in = new BufferedReader(new FileReader(workingDir+chem_inp));
		PrintWriter out = new PrintWriter(new FileWriter(workingDir+"temp.inp"));
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
		for (int i = 0; i < fix_reactions.length; i++){
			dummy = in.readLine();
			String[] st_dummy = dummy.split("\\s");
			for (int j = 0; j < fix_reactions[0].length; j++){
				//put new values of kinetic parameters (at position 1, 2, 3 of st_dummy[]):
				st_dummy[j+1] = Double.toString(dummy_beta_new[counter]);
				counter++;
			}
			
			dummy = st_dummy[0];
			for (int j = 1; j < st_dummy.length; j++) {
				dummy = dummy +" "+st_dummy[j];
			}
			System.out.println(dummy);
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
		
		File f_old = new File(workingDir+chem_inp);
		f_old.delete();
		File f = new File(workingDir+"temp.inp");
		f.renameTo(new File(workingDir+chem_inp));
	}
	
	/**
	 * retrieve_fitted_parameters returns the parameters that are loose (containing a 1 in the dummy_fix_reactions array)
	 * @return
	 */
	public double[] retrieve_fitted_parameters(){
		//count the number of fitted parameters:
		int no_fitted_parameters = 0;
		for (int i = 0; i < fix_reactions.length; i++){
			for (int j = 0; j < fix_reactions[0].length; j++){
				no_fitted_parameters += fix_reactions[i][j];
			}
		}
		
		double [] fitted_parameters = new double[no_fitted_parameters];
		int counter = 0;
		for (int i = 0; i < dummy_beta.length; i++){
			if (dummy_fix_reactions[i]==1){
				fitted_parameters[counter] = dummy_beta[i];
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
		for (int i = 0; i < dummy_beta.length; i++){
			if (dummy_fix_reactions[i]==1){
				dummy_beta[i] = d[counter];
				counter++;
			}
		}
		return dummy_beta;
	}

	public NBMTHost getNBMTHost(){
		return nbmthost;
	}
	public void calcStatistics() throws Exception{
		convert2Dto1D();
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
			moveFile(outputDir, "statistics.txt");
	}

	public boolean isWeighted_regression() {
		return weightedRegression;
	}

}
