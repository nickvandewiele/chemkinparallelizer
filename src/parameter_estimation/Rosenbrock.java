package parameter_estimation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Rosenbrock algorithm developed by Nick Vandewiele, February 2010 <BR>
 * inspired by B. Debrabandere's fortran implementation <BR>
 * adopted from "Computational Techniques for Chemical Engineers", by H.H. Rosenbrock and C. Storey <BR>
 * Ghent University <BR>
 * The Rosenbrock type performs the actual parameter optimization. Function type is called to calculate the Sum of Squared Errors (SSQ) <BR>
 */
public class Rosenbrock extends Paths{

	/**
	 * @param args
	 */
	
	public int maxeval;
	public double efrac = 0.3;
	private double [][] basis;
	private double [][] e;
	
	//multiplicator of step size if succes (trial < current) occurred:
	public double succ = 3.0;
	//multiplicator of step size if failure (trial > current) occurred:
	public double fail = -0.5;

	// all parameter related structures are modeled as double [][]matrices with rows equal to the number of fitted reactions, columns equal to the number of parameters per reaction (e.g. modified Arrhenius = 3)
	public double [][] beta_old;
	public double [][] beta_new;
	
	public double [][] beta_min;
	public double [][] beta_max;
	public int [][] fix_reactions;
	
	//constructor:
	public Rosenbrock (String wd, String cd, int m, double [][] b_old, String[] r_inp, int no_lic, String c_inp, double [][] b_min, double [][] b_max, int [][] f_rxns){
		super(wd, cd, c_inp, r_inp, no_lic);
		maxeval = m;
		beta_old = b_old;
		beta_min = b_min;
		beta_max = b_max;
		fix_reactions = f_rxns;
		
		beta_new = new double [b_old.length][b_old[0].length];

		e = new double [b_old.length][b_old[0].length];
		for (int i = 0; i < b_old.length; i++) {
			for (int j = 0; j < b_old[0].length; j++){
				e[i][j] = beta_old[i][j] * efrac;	
			}
		}
	}
		
	public double [][] getOptimizedParams(List<Map<String,Double>> exp) throws IOException{
		
		//for programming ease of the optimization algorithm, the double [][] matrix b_old (containing parameters per reaction) will be converted to a 1D vector:
		
		int total_no_parameters = fix_reactions.length * fix_reactions[0].length;
		
		int [] dummy_fix_reactions = new int[total_no_parameters];
		double [] dummy_beta_new = new double[total_no_parameters];
		double [] dummy_beta_old = new double[total_no_parameters];
		double [] dummy_e = new double[total_no_parameters];
		double [] dummy_beta_min = new double[total_no_parameters];
		double [] dummy_beta_max = new double[total_no_parameters];
		
		//copy values of [][] matrices to [] vectors:
		int counter = 0;
		for (int i = 0; i < fix_reactions.length; i++) {
			for (int j = 0; j < fix_reactions[0].length; j++){
				dummy_fix_reactions[counter] = fix_reactions[i][j];
				dummy_beta_new[counter] = beta_new[i][j];
				dummy_beta_old[counter] = beta_old[i][j];
				dummy_e[counter] = e[i][j];
				dummy_beta_min[counter] = beta_min[i][j];
				dummy_beta_max[counter] = beta_max[i][j];
				counter++;
			}
		}
		
		//basis needs to be declared with the correct dimensions:
		basis = new double [dummy_beta_old.length][dummy_beta_old.length];
		
		//actual Rosenbrock optimization starts here:
		PrintWriter out = new PrintWriter(new FileWriter("output.txt"));
		PrintWriter out_SSQ = new PrintWriter (new FileWriter("SSQ.csv"));
		int neval = 1;
		out.println("Current evaluation no.: "+neval);

		// initialization of basis: OK
		basis = basis_init(basis);
		
		boolean flag_CKSolnList = true;
		
		//evaluate model predictions with initial guesses:
		List<Map<String,Double>> model = new ArrayList<Map<String,Double>>();
		CKPackager ckp = new CKPackager(workingDir, chemkinDir, chem_inp, reactor_inputs, no_licenses, flag_CKSolnList);
		model = ckp.getModelValues();
		
		//set flag_CKSolnList to false to prevent calling the CKSolnList creator once again:
		flag_CKSolnList = false;
		
		// function evaluation in initial point
		Function f = new Function(model,exp);
		//even in the initial point, one already has model values, error variance matrix can thus be taken, not just response variables
		double initial = f.return_SSQ();
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
		while (neval < maxeval) {
			for (int i = 0; i < dummy_beta_old.length; i++) {
				if (dummy_fix_reactions[i]==1){
					System.out.println("Beta: ");
					print(dummy_beta_old);
					
					//Parameters are slightly changed in the direction of basisvector 'i' to beta_new(j), j=1..np
					for (int j = 0; j < dummy_beta_new.length; j++) {
						dummy_beta_new[j] = dummy_beta_old[j] + dummy_e[i]*basis[j][i];
						if (dummy_beta_new[j] < dummy_beta_min[j]) {
							out.println("New parameter guess has exceeded user-defined lower limits!");
							out.println("new guesses will be equal to user-defined lower limits");
							dummy_beta_new[j] = dummy_beta_min[j];
						}
						if (dummy_beta_new[j] > dummy_beta_max[j]) {
							out.println("New parameter guess has exceeded user-defined upper limits!");
							out.println("new guesses will be equal to user-defined upper limits");
							dummy_beta_new[j] = dummy_beta_max[j];
						}
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
					update_chemistry_input(dummy_beta_new);
					CKPackager ckp_new = new CKPackager(workingDir, chemkinDir, chem_inp, reactor_inputs, no_licenses, flag_CKSolnList);
					model = ckp_new.getModelValues();
					
					//Evaluate (value 'trial') cost function with new parameter guesses [beta_new(j)]
					Function f_new = new Function(model,exp);
					double trial = f_new.return_SSQ();
					out.println("Trial SSQ: "+trial);
					if(trial < current){
						out.println("Woohoo! trial < current!");
						out.println("Old SSQ: "+current);
						out.println("New SSQ: "+trial);
						out_SSQ.println(neval+","+trial);
						for (int j = 0; j < dummy_beta_old.length; j++) {
							dummy_beta_old[j] = dummy_beta_new[j];
						}
						current = trial;
						for (int j = 0; j < d.length; j++) {
							d[j] = d[j] + dummy_e[j];
						}
						dummy_e[i] = succ * dummy_e[i];
						// If flag(i) .EQ. 1, at least one success has occurred along direction i
						if (flag[i] == 2) {
							flag[i] = 1;
						}
					}
					else {
						out.println("Damn. trial SSQ > current SSQ...");
						out_SSQ.println(neval+","+trial);
						dummy_e[i] = fail * dummy_e[i];
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
		moveFile(outputDir, "output.txt");
		moveFile(outputDir, "SSQ.csv");
		
		//convert 1D vector back to matrix [][] notation:
		counter = 0;
		for (int i = 0; i < fix_reactions.length; i++) {
			for (int j = 0; j < fix_reactions[0].length; j++){
				beta_old[i][j] = dummy_beta_old[counter];
				counter++;
			}
		}
		return beta_old;
	}
	/**	
	 * plug new parameter guesses into the chemkin chemistry input file (usually chem.inp)<BR>
	 * write new update chem.inp file <BR>
	 * return the chemistry input filename<BR>
	 * WARNING: method supposes a pre-conditioned chem.inp file, processed by ChemClean and with TD inside chem.inp!!!<BR>
	 */
		public void update_chemistry_input (double [] dummy_beta_new) throws IOException{
			BufferedReader in = new BufferedReader(new FileReader(workingDir+chem_inp));
			PrintWriter out = new PrintWriter(new FileWriter(workingDir+"temp.inp"));
			String dummy = in.readLine();
			//just copy part of chem.inp about Elements, Species, Thermo
			/**
			 * TODO
			 * string "REACTIONS	KJOULES/MOLE	MOLES" is not robust enough, must be more generic
			 */
			boolean b = true;
			while(b){
			//while(!dummy.equals("REACTIONS	KJOULES/MOLE	MOLES")){
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
			

//the while loop will become obsolete:
/*			
			while(!dummy.equals("END")){
				String[] st_dummy = dummy.split("\\s");
				st_dummy[1] = Double.toString(beta_new[i]);
				i++;
				//System.out.println(i);
				st_dummy[3] = Double.toString(beta_new[i]);
				i++;
				//System.out.println(i);
				//toString(st_dummy);
				dummy = st_dummy[0];
				for (int j = 1; j < st_dummy.length; j++) {
					dummy = dummy +" "+st_dummy[j];
				}
				System.out.println(dummy);
				out.println(dummy);
				dummy = in.readLine();
			}		
			// write "END" line:
			out.println(dummy);
*/
			//delete old chem.inp file and create new chem.inp based on temp.inp:
			File f_old = new File(workingDir+chem_inp);
			f_old.delete();
			File f = new File(workingDir+"temp.inp");
			f.renameTo(new File(workingDir+chem_inp));
		}
		
		/**
		 * Initialization of the basis, taking unit vectors in every direction of the parameters
		 */
	public double[][] basis_init (double [][] bbasis) {
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
	 */
	public double[][] set_new_basis (double [] dd, double [][]bbasis){
	
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
	 * @param basis
	 * @return An orthonormal basis is derived and returned from the matrix basis using the Gram-Schmidt algorithm
	 */
	public double[][] gramschmidt (double [][] bbasis) {
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
	 * @param flag
	 * @return all flags are set to 2, meaning i.e. no success has been achieved in direction i
	 */
	public int [] reset_flag (int fflag[]){
		for (int i = 0; i < fflag.length; i++) {
			fflag[i] = 2;
		}
		return fflag;
	}
	
	public double [] reset_d (double dd[]){
		for (int i = 0; i < dd.length; i++) {
			dd[i] = 0;
		}
		return dd;
	}
	public static void print(double [] d){
		for (int i = 0; i < d.length; i++) {
			System.out.print(d[i]+" ");
		}
		System.out.println();
	}
}
