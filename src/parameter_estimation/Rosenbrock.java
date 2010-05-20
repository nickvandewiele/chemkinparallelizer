package parameter_estimation;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
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
	/**
	 * @param args
	 */
	public int maxeval;
	public double EFRAC; //rosenbrock parameter
	public double SUCC; //rosenbrock parameter
	public double FAIL; //rosenbrock parameter
	public double[][] basis;

	public int total_no_parameters;

	public double[] dummy_beta_old;
	public double[] dummy_beta_new;
	public double[] dummy_beta_min;
	public double[] dummy_beta_max;
	/**
	 * TODO the fix_reactions vector should not be part of Rosenbrock anymore. do it like LM!
	 */
	public int[] dummy_fix_reactions;
	
	public double[] dummy_e;
	
	/**
	 * Optimization type is used by Rosenbrock as server class, from which methods like Optimization.getModelValues are taken
	 * and from which attributes like all the 1D vectors are copied (throught the Optimization.getters)
	 * this solution is not that elegant as it requires to add (hard-code) Optimization attribute in new optimization routines.
	 * this requires to mess in the external source code...
	 * TODO: how to create an optimization code that does not require the addition of Optimization type in its source code? 
	 */
	private Optimization optimization;

	public Rosenbrock(Optimization o, double efrac, double succ, double fail, int maxeval) {
		optimization = o;
		this.EFRAC = efrac;
		this.SUCC = succ;
		this.FAIL = fail;
		this.maxeval = maxeval;
		this.dummy_beta_old = o.get_dummy_beta_old();
		this.dummy_beta_new = new double[dummy_beta_old.length];
		this.dummy_beta_min = o.get_dummy_beta_min();
		this.dummy_beta_max = o.get_dummy_beta_max();
		this.dummy_fix_reactions = o.get_dummy_fix_reactions();
		this.dummy_e = new double[o.get_total_no_parameters()];
		for (int i = 0; i < dummy_beta_old.length; i++) {
				dummy_e[i] = dummy_beta_old[i] * EFRAC;	
		}
	}

	public double [] return_optimized_parameters() throws Exception{
		//basis needs to be declared with the correct dimensions:
		basis = new double [dummy_beta_old.length][dummy_beta_old.length];

		PrintWriter out = new PrintWriter(new FileWriter("output_Rosenbrock.txt"));
		PrintWriter out_SSQ = new PrintWriter (new FileWriter("SSQ_Rosenbrock.csv"));
		int neval = 1;
		out.println("Current evaluation no.: "+neval);

		// initialization of basis: OK
		basis = basis_init(basis);
		
		//evaluate model predictions with initial guesses:
		//flag_CKSolnList = true
		List<Map<String,Double>> model = optimization.getModelValues(dummy_beta_old,true);
		
		// function evaluation in initial point
		Function f = new Function(model,optimization.getExp());
		
		//even in the initial point, one already has model values, error variance matrix can thus be taken, not just response variables
		double initial = f.getSSQ();
		System.out.println("Initial SSQ: "+initial);
		double current = initial;
		System.out.println("Current value: "+current);
		out_SSQ.println(neval+","+initial);
		
		// Set all flags to 2, i.e. no success has been achieved in direction i
		int [] flag = new int [dummy_beta_old.length];
		double [] d = new double [dummy_beta_old.length];
		flag = reset_flag(flag);
		d = reset_d(d);
		
		// Main rosenbrock loop
		while (neval < optimization.maxeval) {
			for (int i = 0; i < dummy_beta_old.length; i++) {
				if (dummy_fix_reactions[i]==1){
					System.out.println("Beta: ");
					print(dummy_beta_old);
					
					//Parameters are slightly changed in the direction of basisvector 'i' to beta_new(j), j=1..np
					for (int j = 0; j < dummy_beta_new.length; j++) {

						//new parameter trials:
						dummy_beta_new[j] = dummy_beta_old[j] + dummy_e[i]*basis[j][i];
						dummy_beta_new[j] = check_lower_upper_bounds(dummy_beta_new[j], dummy_beta_min[j], dummy_beta_max[j], out);
						
					}
					
					System.out.println("Beta new (to be tested): ");
					print(dummy_beta_new);
					
					//print new parameter guesses to file: 
					for (int l = 0; l < dummy_beta_new.length; l++) {
						out.print(dummy_beta_new[l]+", ");
					}
					out.println();
					
					//Number of evaluations is updated:
					neval++;
					
					out.println("Current evaluation no.: "+neval);
					System.out.println("Evaluation no. "+neval);
					
					//model predictions with new parameter guesses is called:
					//set flag_CKSolnList to false to prevent calling the CKSolnList creator once again:
					//flag_CKSolnList = false
					model = optimization.getModelValues(dummy_beta_new,false);
				
					//Evaluate (value 'trial') cost function with new parameter guesses [beta_new(j)]
					Function f_new = new Function(model,optimization.getExp());
					double trial = f_new.getSSQ();
					
					out.println("Trial SSQ: "+trial);
					if(trial < current){
						out.println("Woohoo! trial < current!");
						out.println("Old SSQ: "+current);
						out.println("New SSQ: "+trial);
						out_SSQ.println(neval+","+trial);
						
						//put new successful parameter guesses in the old ones, which will eventually be returned
						for (int j = 0; j < dummy_beta_old.length; j++) {
							dummy_beta_old[j] = dummy_beta_new[j];
						}
						
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
						out_SSQ.println(neval+","+trial);
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
							basis = set_new_basis(d,basis);
							basis = gramschmidt(basis);
							flag = reset_flag(flag);
							d = reset_d(d);
						}
					}
				}
				
				//If number of evaluations exceeds maxeval, jump out of 'for' loop and evaluate once more:
				if (neval >= maxeval){
					i = dummy_beta_old.length;
				}
			}
		}
		
		out.close();
		out_SSQ.close();
				
		return dummy_beta_old;
	}
	/**
	 * Initialization of the basis, taking unit vectors in every direction of the parameters

	 * @param bbasis
	 */
	public double[][] basis_init (double[][] bbasis) {
	for (int i = 0; i < bbasis[0].length; i++){
		for (int j = 0; j < bbasis[0].length; j++) {
			bbasis[i][j] = 0.0;
		}
		bbasis[i][i] = 1.0;
	}
	return bbasis;
	}

	/**
	* If successes have been found in multiple directions, this means that a new basis should be chosen<BR>
	* Basis vectors should be chosen in the joint direction of the successes<BR>
	* This method sets a new basis, but the actual implementation is still a mystery to me<BR>
	 * @param dd
	 * @param bbasis
	*/
	public double[][] set_new_basis (double[] dd, double[][] bbasis){
		
	for (int i = 0; i < dd.length; i++) {
		for (int j = 0; j < dd.length; j++) {
			bbasis[j][i] = dd[i] * bbasis[j][i];
		}
		for (int k = i+1; k < dd.length; k++) {
			for (int j = 0; j < dd.length; j++) {
				bbasis[j][i] = bbasis[j][i] + dd[k] * bbasis[j][k];	
			}
		}
	}
	return bbasis;
	}

	/**
	* 
	* @param bbasis
	* @return An orthonormal basis is derived and returned from the matrix basis using the Gram-Schmidt algorithm
	*/
	public double[][] gramschmidt (double[][] bbasis) {
	//  gram schmidt orthonormalization
	
	for(int  i = 0; i < bbasis[0].length; i++){
		for(int k = 0; k < i-1; k++){
			double scal_prod = 0.0;
			for (int j = 0; j < bbasis[0].length; j++){
				scal_prod = scal_prod + bbasis[j][i] * bbasis[j][k];
			}
			for (int j = 0; j < bbasis[0].length; j++) {
				bbasis[j][i] = bbasis[j][i] - scal_prod * bbasis[j][k];
			}
		}
	// calculation of norms of every basis vector: 
		double norm = 0.0;
		for (int j = 0; j < basis[0].length; j++){
			norm = norm + basis[j][i] * basis[j][i];
		}
	// normalization of new bases:          
		for (int j = 0; j < basis[0].length; j++){
			basis[j][i] = basis[j][i] / Math.sqrt(norm);
		}
	}
	//  nit = nit+1;
	     
	return bbasis;
	}

	/**
	* 
	* @param fflag
	 * @return all flags are set to 2, meaning i.e. no success has been achieved in direction i
	*/
	public int [] reset_flag (int[] fflag){
		for (int i = 0; i < fflag.length; i++) {
			fflag[i] = 2;
		}
		return fflag;
	}

	public double [] reset_d (double[] dd){
		for (int i = 0; i < dd.length; i++) {
			dd[i] = 0;
		}
		return dd;
	}

	public double check_lower_upper_bounds(double d, double lower, double upper, PrintWriter out){
		double dummy = d; 
		if (d < lower) {
			out.println("New parameter guess has exceeded user-defined lower limits!");
			out.println("new guesses will be equal to user-defined lower limits");
			dummy = lower;
		}
		else if (d > upper) {
			out.println("New parameter guess has exceeded user-defined upper limits!");
			out.println("new guesses will be equal to user-defined upper limits");
			dummy = upper;
		}
		else {
			//do nothing
		}
		return dummy;
	}	
	
	public static void print(double [] d){
		for (int i = 0; i < d.length; i++) {
			System.out.print(d[i]+" ");
		}
		System.out.println();
	}
}