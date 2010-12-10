package parameter_estimation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
/**
 * Rosenbrock algorithm developed by Nick Vandewiele, February 2010 <BR>
 * inspired by B. Debrabandere's fortran implementation <BR>
 * adopted from "Computational Techniques for Chemical Engineers", by H.H. Rosenbrock and C. Storey <BR>
 * Ghent University <BR>
 * The Rosenbrock type performs the actual parameter optimization. Function type is called to calculate the Sum of Squared Errors (SSQ) <BR>
 * 
 * Rosenbrock algorithm uses two types as server classes:
 * 	-Optimization: provides getModelValues method (that calls update_chemistry_input to adjust chemistry input file) and 
 *   attributes like total_no_parameters, maxeval. Rosenbrock delegates this to Optimization type
 * 	-Function
 */

public class Rosenbrock{
	static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
	private double EFRAC; //rosenbrock parameter
	private double SUCC; //rosenbrock parameter
	private double FAIL; //rosenbrock parameter
	public double[][] basis;

	public double[] dummy_beta_new;

	/**
	 * TODO the fix_reactions vector should not be part of Rosenbrock anymore. do it like LM!
	 */
	public double[] dummy_e;
	
	/**
	 * Optimization type is used by Rosenbrock as server class, from which methods like Optimization.getModelValues are taken
	 * and from which attributes like all the 1D vectors are copied (throught the Optimization.getters)
	 * this solution is not that elegant as it requires to add (hard-code) Optimization attribute in new optimization routines.
	 * this requires to mess in the external source code...
	 * TODO: how to create an optimization code that does not require the addition of Optimization type in its source code? 
	 */
	private Optimization optimization;
	private ModelValues modelValues;
	public Rosenbrock(Optimization o, double efrac, double succ, double fail) {
		optimization = o;
		this.EFRAC = efrac;
		this.SUCC = succ;
		this.FAIL = fail;

		this.dummy_beta_new = new double[o.getParams1D().getBeta().length];

		this.dummy_e = new double[o.getParams1D().getBeta().length];
		for (int i = 0; i < o.getParams1D().getBeta().length; i++) {
				dummy_e[i] = o.getParams1D().getBeta()[i] * EFRAC;	
		}
	}

	public double [] returnOptimizedParameters() throws Exception{
		//basis needs to be declared with the correct dimensions:
		basis = new double [optimization.getParams1D().getBeta().length][optimization.getParams1D().getBeta().length];

		PrintWriter out = new PrintWriter(new FileWriter("output_Rosenbrock.txt"));
		PrintWriter outSSQ = new PrintWriter (new FileWriter("SSQ_Rosenbrock.csv"));
		int neval = 1;
		out.println("Current evaluation no.: "+neval);

		// initialization of basis: OK
		basis = Algebra.basisInit(basis);
		
		//evaluate model predictions with initial guesses:
		//flag_CKSolnList = true
		modelValues = optimization.testNewParameters(optimization.getParams1D().getBeta(),true); 

		// function evaluation in initial point
		Function f = new Function(optimization.getExperiments(),modelValues);
		
		//even in the initial point, one already has model values, error variance matrix can thus be taken, not just response variables
		double initial = f.getSRES();
		logger.info("Initial SSQ: "+initial);
		double current = initial;
		logger.info("Current value: "+current);
		outSSQ.println(neval+","+initial);
		
		// Set all flags to 2, i.e. no success has been achieved in direction i
		int [] flag = new int [optimization.getParams1D().getBeta().length];
		double [] d = new double [optimization.getParams1D().getBeta().length];
		flag = Rosenbrock.resetFlag(flag);
		d = Rosenbrock.resetD(d);
		
		Function fNew;
		// Main rosenbrock loop
		while (neval < optimization.getFitting().getMaxNoEvaluations()) {
			for (int i = 0; i < optimization.getParams1D().getBeta().length; i++) {
				if (optimization.getParams1D().getFixRxns()[i]==1){
					logger.info("Beta: ");
					print(optimization.getParams1D().getBeta());
					
					//Parameters are slightly changed in the direction of basisvector 'i' to beta_new(j), j=1..np
					for (int j = 0; j < optimization.getParams1D().getBeta().length; j++) {

						//new parameter trials:
						dummy_beta_new[j] = optimization.getParams1D().getBeta()[j] + dummy_e[i]*basis[j][i];
						dummy_beta_new[j] = Algebra.checkLowerUpperBounds(dummy_beta_new[j],
								optimization.getParams1D().getBetamin()[j],
								optimization.getParams1D().getBetamax()[j],
								out);	
					}
					
					logger.info("Beta new (to be tested): ");
					print(dummy_beta_new);
					
					//print new parameter guesses to file: 
					for (int l = 0; l < dummy_beta_new.length; l++) {
						out.print(dummy_beta_new[l]+", ");
					}
					out.println();
					
					//Number of evaluations is updated:
					neval++;
					
					out.println("Current evaluation no.: "+neval);
					logger.info("Evaluation no. "+neval);
					
					//model predictions with new parameter guesses is called:
					//set flag_CKSolnList to false to prevent calling the CKSolnList creator once again:
					//flag_CKSolnList = false
					ModelValues modelValuesTest = optimization.testNewParameters(dummy_beta_new,false);
				
					//Evaluate (value 'trial') cost function with new parameter guesses [beta_new(j)]
					
					//fNew = new Function(optimization.getExperiments(),optimization.getModelValues());
					fNew = new Function(optimization.getExperiments(),modelValuesTest);
					double trial = fNew.getSRES();
					
					out.println("Trial SSQ: "+trial);
					if(trial < current){
						out.println("Woohoo! trial < current!");
						out.println("Old SSQ: "+current);
						out.println("New SSQ: "+trial);
						outSSQ.println(neval+","+trial);
						
						//put new successful parameter guesses in the old ones, which will eventually be returned
/*						for (int j = 0; j < dummy_beta_old.length; j++) {
							dummy_beta_old[j] = dummy_beta_new[j];
						}
*/						double []succesFulParameters = new double[dummy_beta_new.length]; 
						System.arraycopy(dummy_beta_new, 0, succesFulParameters, 0, succesFulParameters.length);
						optimization.getParams1D().setBeta(succesFulParameters);
						
						current = trial;
						for (int j = 0; j < d.length; j++) {
							d[j] = d[j] + dummy_e[j];
						}
						dummy_e[i] = SUCC * dummy_e[i];
						// If flag(i) .EQ. 1, at least one success has occurred along direction i
						if (flag[i] == 2) {
							flag[i] = 1;
						}
					}
					else {
						out.println("Damn. trial SSQ > current SSQ...");
						outSSQ.println(neval+","+trial);
						dummy_e[i] = FAIL * dummy_e[i];
						//If flag(i) == 0, at least one success has been followed by a least one failure in direction i.
						if (flag[i] == 1){
							flag[i] = 0;
						}
						//Test for condition "success followed by failure" for each direction.  If flag2 == 0, the test is positive.
						int flag2 = 0;
						for (int j = 0; j < flag.length; j++) {
							flag2 = flag2 + flag[j];
						}
						if (flag2 == 0){
							basis = Algebra.setNewBasis(d,basis);
							basis = Algebra.gramschmidt(basis);
							flag = resetFlag(flag);
							d = Rosenbrock.resetD(d);
						}
					}
				}
				
				//If number of evaluations exceeds maxeval, jump out of 'for' loop and evaluate once more:
				if (neval >= optimization.getFitting().getMaxNoEvaluations()){
					i = optimization.getParams1D().getBeta().length;
				}
			}
		}
		
		out.close();
		outSSQ.close();
				
		return optimization.getParams1D().getBeta();
	}
	public static double [] resetD (double[] dd){
		for (int i = 0; i < dd.length; i++) {
			dd[i] = 0;
		}
		return dd;
	}

	/**
	* 
	* @param fflag
	 * @return all flags are set to 2, meaning i.e. no success has been achieved in direction i
	*/
	public static int [] resetFlag (int[] fflag){
		for (int i = 0; i < fflag.length; i++) {
			fflag[i] = 2;
		}
		return fflag;
	}

	public static void print(double [] d){
		for (int i = 0; i < d.length; i++) {
			logger.info(d[i]+" ");
		}
		
	}
}