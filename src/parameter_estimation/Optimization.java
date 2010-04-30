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
public class Optimization extends Paths{
	public int maxeval;
	public double[][] beta_old;
	public double[][] beta_new;
	public double[][] beta_min;
	public double[][] beta_max;
	public int[][] fix_reactions;
	

	public double[] dummy_beta_old;
	public double[] dummy_beta_new;
	public double[] dummy_beta_min;
	public double[] dummy_beta_max;
	public int[] dummy_fix_reactions;
	
	public int total_no_parameters;

	/**
	 * Rosenbrock serves as server class to Optimization.
	 * it will execute the actual optimization
	 */
	public Rosenbrock rosenbrock;

	//constructor:
	public Optimization (String wd, String cd, int m, double [][] b_old, String[] r_inp, int no_lic, String c_inp, double [][] b_min, double [][] b_max, int [][] f_rxns){
		super(wd, cd, c_inp, r_inp, no_lic);
		maxeval = m;
		beta_old = b_old;
		beta_new = new double [b_old.length][b_old[0].length];
		beta_min = b_min;
		beta_max = b_max;
		fix_reactions = f_rxns;	
	}
	
	/**
	 * convert2D_to_1D() will initialize the 1D vectors derived from the 2D matrices containing parameter data to be used in optimization routine
	 */
	public void convert_2D_to_1D(){
		//for programming ease of the optimization algorithm, the double [][] matrix b_old (containing parameters per reaction)
		//will be converted to a 1D vector:
		
		//initialize new 1D vectors:
		total_no_parameters = fix_reactions.length * fix_reactions[0].length;
	
		dummy_beta_old = new double[total_no_parameters];
		dummy_beta_new = new double[total_no_parameters];
		dummy_beta_min = new double[total_no_parameters];
		dummy_beta_max = new double[total_no_parameters];
		dummy_fix_reactions = new int[total_no_parameters];
			
		//copy values of [][] matrices to [] vectors:
		int counter = 0;
		for (int i = 0; i < fix_reactions.length; i++) {
			for (int j = 0; j < fix_reactions[0].length; j++){
				dummy_beta_old[counter] = beta_old[i][j];
				dummy_beta_new[counter] = beta_new[i][j];
				dummy_beta_min[counter] = beta_min[i][j];
				dummy_beta_max[counter] = beta_max[i][j];
				dummy_fix_reactions[counter] = fix_reactions[i][j];				
				counter++;
			}
		}
	}

	
	public double [][] optimize(List<Map<String,Double>> exp) throws Exception{
		
		convert_2D_to_1D();
		
		//Rosenbrock parameters:
		double efrac = 0.3;
		double succ = 3.0;
		double fail = -0.5;
		
		rosenbrock = new Rosenbrock(this, efrac, succ, fail);
		beta_new = convert_1D_to_2D(rosenbrock.return_optimized_parameters(exp));
		
		moveFile(outputDir, "output.txt");
		moveFile(outputDir, "SSQ.csv");
		
		return beta_new;
	}
	
	/**
	 * convert 1D vector into 2D matrix: 
	 * @return
	 */
	public double [][] convert_1D_to_2D(double [] vector){
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
	public double [] get_dummy_beta_old(){
		return dummy_beta_old;
	}
	public double [] get_dummy_beta_new(){
		return dummy_beta_new;
	}
	public double [] get_dummy_beta_min(){
		return dummy_beta_min;
	}
	public double [] get_dummy_beta_max(){
		return dummy_beta_max;
	}
	public int [] get_dummy_fix_reactions(){
		return dummy_fix_reactions;
	}
	
	public int get_total_no_parameters(){
		return total_no_parameters;
	}
	
	public List<Map<String,Double>> getModelValues(double [] parameter_guesses, boolean flag_CKSolnList) throws Exception{
		//update_chemistry_input will insert new parameter_guesses array into chem_inp
		update_chemistry_input(parameter_guesses);
		
		List<Map<String,Double>> model = new ArrayList<Map<String,Double>>();
		CKPackager ckp_new = new CKPackager(workingDir, chemkinDir, chem_inp, reactor_inputs, no_licenses, flag_CKSolnList);
		model = ckp_new.getModelValues();
		return model;
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
		
		File f_old = new File(workingDir+chem_inp);
		f_old.delete();
		File f = new File(workingDir+"temp.inp");
		f.renameTo(new File(workingDir+chem_inp));

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
	}
	
}
