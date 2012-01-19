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

import datatypes.ModelValue;

import parameter_estimation.levenberg.NBMTHost;
import parsers.ConfigurationInput;

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
public class Optimization extends Loggable{
	
	public ConfigurationInput config;
	
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
	public Optimization (ConfigurationInput config){
		
		beta_new = new double [config.chemistry.getParams().getBeta().length][config.chemistry.getParams().getBeta()[0].length];		
//		weighted_regression = w_r;
	}
	
	
	public double [][] optimize() throws Exception{
		params1D = new Parameters1D();
		params1D.convert2Dto1D(config.chemistry.getParams());
		if(config.fitting.method.equals(Fitting.ROSENBROCK)){
			//Rosenbrock parameters:
			double efrac = 0.5;//0.3
			double succ = 3.0;
			double fail = -0.5;
			logger.info("Start of Rosenbrock!");
			rosenbrock = new Rosenbrock(this, efrac, succ, fail);
			double [] optimizedParameters = rosenbrock.returnOptimizedParameters(); 
			beta_new = Tools.convert1Dto2D(optimizedParameters, config.chemistry.getParams().getBeta());
		}
		if(config.fitting.method.equals(Fitting.LEVENBERG)){
			logger.info("Start of Levenberg-Marquardt!");
			nbmthost = new NBMTHost(this);
			beta_new = Tools.convert1Dto2D(buildFullParamVector(nbmthost.getParms()), config.chemistry.getParams().getBeta());		
		}

		// move Rosenbrock monitors:
		if(new File("SSQ_Rosenbrock.csv").exists())
			Tools.moveFile(config.paths.getOutputDir(), "SSQ_Rosenbrock.csv");
		if(new File("output_Rosenbrock.txt").exists())
			Tools.moveFile(config.paths.getOutputDir(), "output_Rosenbrock.txt");

		//move LM monitors:
		if(new File("LM.txt").exists())
			Tools.moveFile(config.paths.getOutputDir(), "LM.txt");
		if(new File("SSQ_LM.txt").exists())
			Tools.moveFile(config.paths.getOutputDir(), "SSQ_LM.txt");
		if(new File("response_vars.txt").exists())
			Tools.moveFile(config.paths.getOutputDir(), "response_vars.txt");
		return beta_new;
	}
	
	
	public ModelValue[] testNewParameters(double [] parameter_guesses, boolean flag_CKSolnList) throws Exception{
		//update_config.chemistry_input will insert new parameter_guesses array into chem_inp
		Command updateChemistry = new UpdateChemistryCommand(config, parameter_guesses);
		updateChemistry.execute();
		
	
		AbstractCKPackager ckp = new CKPackager(config);
		ckp = new ExtractModelValuesPackagerDecorator(ckp);
		ckp.runAllSimulations();
		ModelValue[] modelValues = ckp.getModelValues();
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
	 * retrieve_fitted_parameters returns the parameters that are loose (containing a 1 in the dummy_fix_reactions array)
	 * @return
	 */
	public double[] retrieveFittedParameters(){
		//count the number of fitted parameters:
		int no_fitted_parameters = 0;
		for (int i = 0; i < config.chemistry.getParams().getFixRxns().length; i++){
			for (int j = 0; j < config.chemistry.getParams().getFixRxns()[0].length; j++){
				no_fitted_parameters += config.chemistry.getParams().getFixRxns()[i][j];
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
		params1D.convert2Dto1D(config.chemistry.getParams());
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
		out.println(config.experiments.experimentalValues.length);
		out.println("Number of fitted parameters:");
		out.println(config.chemistry.getParams().getNoFittedParameters());
		out.println("Number of responses:");
		out.println(config.experiments.experimentalValues.length);
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
			Tools.moveFile(config.paths.getOutputDir(), "statistics.txt");
	}

	public boolean isWeightedRegression() {
		return weightedRegression;
	}



	
	/**
	 * ####################
	 * GETTERS AND SETTERS:
	 * ####################
	 */
	


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
