package parameter_estimation;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ParameterEstimationDriver {
//this comment is added to verify version control system
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		
		//input file will be searched in working directory under the name INPUT.txt:
		BufferedReader in = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/INPUT.txt"));
		
		
		in.readLine();
		String workingDir = in.readLine();
		int wdlength = workingDir.length();
		if (workingDir.charAt(wdlength-1)!='/'){
			System.out.println("Pathname to working directory needs to end with forward slash '/' !");
			System.exit(-1);
		}
		
		in.readLine();
		String chemkinDir = in.readLine();
		
		in.readLine();
		int no_licenses = Integer.parseInt(in.readLine());
		
		in.readLine();
		String chem_inp = in.readLine();
		
		in.readLine();
		String experiments_db = in.readLine();
		
		in.readLine();
		boolean flag_reactor_db = Boolean.parseBoolean(in.readLine());
		
		in.readLine();
		String reactor_setups_db = in.readLine();
		
		in.readLine();
		int no_experiments = Integer.parseInt(in.readLine());
		
		//number of parameters to be fitted:
		in.readLine();
		int no_params = Integer.parseInt(in.readLine());

		//optimization flags:
		in.readLine();
		boolean flag_Rosenbrock = Boolean.parseBoolean(in.readLine());
		in.readLine();
		boolean flag_LM = Boolean.parseBoolean(in.readLine());
		
		in.readLine();
		int maxeval = Integer.parseInt(in.readLine());
		
		in.readLine();
		int mode = Integer.parseInt(in.readLine());

		//REACTOR INPUT FILE NAMES:
		in.readLine();
		
		String [] reactor_inputs = new String[no_experiments];
		if (!flag_reactor_db){
				for (int i = 0; i < no_experiments; i++){
					reactor_inputs[i] = in.readLine(); 
				}
		}
		else {
			in.readLine();
		}
	
		
		//OPTIMIZATION SECTION: modified Arrhenius parameters [A, n, Ea]: 1 = loose, parameter will be varied; 0 = fixed, parameter will be fixed
		in.readLine();
		
		//number of reactions that will be optimized:
		in.readLine();
		int no_fitted_reactions = Integer.parseInt(in.readLine());
		
		//number of parameters per reaction (modified Arrhenius [A,n,Ea]): 3
		int no_parameters_per_reaction = 3;
		
		int [][] fix_reactions = new int [no_fitted_reactions][no_parameters_per_reaction];
		for (int i = 0; i < no_fitted_reactions; i++){
			//comment line with "reaction i: "
			in.readLine();
			// string of 1,0,1:
			String [] s = in.readLine().split(",");
			for (int j = 0; j < no_parameters_per_reaction; j++){
				fix_reactions[i][j] = Integer.parseInt(s[j]);
			}
		}
		boolean flag_no_parameters = check_no_parameters(fix_reactions, no_params);
		if (!flag_no_parameters) {
			System.out.println("Number of parameters to be fitted provided in INPUT.txt does not equal the number of ones you specified in the optimization section in INPUT.txt!");
			System.exit(-1);
		}
		else {
			//do nothing, continue
		}
		
		in.close();
		
		String template = "reactor_input_template.inp";
		
		double [][] beta_min = new double [no_fitted_reactions][no_parameters_per_reaction];
		double [][] beta_max = new double [no_fitted_reactions][no_parameters_per_reaction];
		for (int i = 0; i < no_fitted_reactions; i++) {
			for (int j = 0; j < no_parameters_per_reaction; j++){
				beta_min[i][j]=0;
				beta_max[i][j]=1e20;
			}
		}
		
		if (flag_reactor_db){
			reactor_inputs = reactor_inputs_parser(workingDir, reactor_setups_db, template, no_experiments);
		}
		else {
/*			int[] exp_no = {1,3,5,11,27,29,33,35,37,45,47,49,53,65,69};
			int [] exp_no = {1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39,41,43,45,47,49,51,53,55,57,59,61,63,65,67,69};
			for (int j = 0; j < no_experiments; j++){
				reactor_inputs[j] = "pinanol-raden_p"+exp_no[j]+".inp";	
			}
*/
		}
		
		switch(mode){
			case 0:	System.out.println("PARITY PLOT MODE");
					Param_Est p0 = new Param_Est(workingDir, chemkinDir, chem_inp, reactor_inputs, no_licenses, no_experiments, experiments_db, beta_min, beta_max, maxeval);
					p0.createOutputDir();
					p0.getParity();
					break;
			case 1:	System.out.println("PARAMETER OPTIMIZATION MODE");
					Param_Est p1 = new Param_Est(workingDir, chemkinDir, chem_inp, reactor_inputs, no_licenses, no_experiments, experiments_db, beta_min, beta_max, maxeval, fix_reactions, flag_Rosenbrock, flag_LM);
					p1.createOutputDir();
					p1.optimizeParameters();
					p1.getParity();
					break;
			case 2: System.out.println("EXCEL POSTPROCESSING MODE");
					Param_Est p2 = new Param_Est(workingDir, chemkinDir, chem_inp, reactor_inputs, no_licenses);
					p2.createOutputDir();
					p2.getExcelFiles();
					break;
		}
		long timeTook = (System.currentTimeMillis() - time)/1000;
	    System.out.println("Time needed for this program to finish: (sec) "+timeTook);
	}
	public static String[] reactor_inputs_parser(String workingDir, String experiments, String template, int no_experiments) throws Exception{
		ArrayList<String> reactor_inputs = new ArrayList<String>();
		
	//read first line of excel input:
		/*
		 * Reading the first line will reveal:<BR>
		 * <LI>the number and names of species at the reactor inlet</LI>
		 * <LI>the T profile</LI>
		 * <LI>the P profile</LI>		
		 */
		BufferedReader in_excel = new BufferedReader(new FileReader(workingDir+experiments));
		String [] reactor_dim = in_excel.readLine().split(",");
		double convert_m_cm = 100;
		double length = Double.parseDouble(reactor_dim[1])*convert_m_cm;
		double diameter = Double.parseDouble(reactor_dim[2])*convert_m_cm;
		
		String [] dummy = in_excel.readLine().split(",");
		
//		NOS : number of species
		int NOS = dummy.length-1;
		ArrayList<String> species_name = new ArrayList<String>();
		for (int i = 0; i < NOS; i++){
			species_name.add(dummy[1+i]);
		}
		
		dummy = in_excel.readLine().split(",");
		ArrayList<Double> species_mw = new ArrayList<Double>();
		for (int i = 0; i < NOS; i++){
			species_mw.add(Double.parseDouble(dummy[1+i]));
		}
		
		String [] exp = in_excel.readLine().split(",");

		int counter=0;
		while(!exp[counter].equals("TEMP")){
			counter++;
		}
		int position_T_profile = counter;
		counter++;
		
		//start reading the axial positions of the Temperature Profile:
		ArrayList<Double> array_Tprofile = new ArrayList<Double>();
		while(!exp[counter].equals("PRESS")){
			array_Tprofile.add(Double.parseDouble(exp[counter])*convert_m_cm);
			counter++;
		}
		int position_P_profile = counter;
		counter++;
		
		//start reading the axial positions of the Pressure Profile:
		ArrayList<Double> array_Pprofile = new ArrayList<Double>();
		for (int i = counter; i < exp.length; i++){
			array_Pprofile.add(Double.parseDouble(exp[i])*convert_m_cm);
		}
/*
 * Start writing the actual reactor input file, by doing the following:<BR>
 * <LI>read in the lines of the template reactor input file that remain unchanged. write them to your output file</LI>
 * <LI>change total mass flow rate</LI>
 * <LI>add Pressure profile</LI>
 * <LI>add Temperature profile</LI>
 * <LI>add diameter</LI>
 */
		
		int experiment_counter = 0;
		String line = null;
		try {
			line = in_excel.readLine();
			while(!dummy.equals(null)){				
				BufferedReader in_template = new BufferedReader(new FileReader(workingDir+template));
				String [] dummy_array = line.split(",");
				//experiment_counter contains the experiment number that will be used in the reactor input file name:
				experiment_counter = Integer.parseInt(dummy_array[0]);
				String filename = "reactor_input_"+experiment_counter+".inp";
				reactor_inputs.add(filename);
				PrintWriter out = new PrintWriter(new FileWriter(workingDir+filename));
				
				//copy the first 7 lines:
				for(int i = 0 ; i < 7 ; i++){
					String d = in_template.readLine();
					out.println(d);
				}
				
				//total mass flow rate:
				double massflrt = 0.0;
				double molflrt = 0.0;
				for(int i = 0 ; i < NOS; i++){
					massflrt=massflrt + Double.parseDouble(dummy_array[1+i])/3600;
					molflrt=molflrt + Double.parseDouble(dummy_array[1+i])/species_mw.get(i);
				}
				
				out.println("FLRT"+" "+massflrt);
				
				//Pressure Profile:
				double pressure = 0.0;
				double convert_bar_atm = 1.01325;
				for (int i = 0; i < array_Pprofile.size(); i++){
					pressure = Double.parseDouble(dummy_array[position_P_profile+i+1])/convert_bar_atm;
					out.println("PPRO "+array_Pprofile.get(i)+" "+pressure);
				}
				//Temperature Profile:
				double temperature = 0.0;
				double convert_C_K = 273.15;
				for (int i = 0; i < array_Tprofile.size(); i++){
					temperature = Double.parseDouble(dummy_array[position_T_profile+i+1])+convert_C_K;
					out.println("TPRO "+array_Tprofile.get(i)+" "+temperature);
				}
				
				//Diameter:
				out.println("DIAM "+diameter);
				
				//reactor length: 
				out.println("XEND "+length);
				//Inlet Species:
				double molfr = 0.0;
				for(int i = 0 ; i < NOS; i++){
					molfr = (Double.parseDouble(dummy_array[1+i])/species_mw.get(i))/molflrt;
					out.println("REAC "+species_name.get(i)+" "+molfr);
				}

				//force solver to use nonnegative species fractions:
				out.println("NNEG");
				
				//END:
				out.println("END");
				
				in_template.close();
				out.close();
				line = in_excel.readLine();
			}
			in_excel.close();
		}catch (Exception e){}//do nothing: e catches the end of the file exception

		// verify the correct number of lines in reactor input file:
		if( reactor_inputs.size()!= no_experiments){
			System.out.println("Number of experiments in reactor inputs file does not correspond to the number of experiments provided in the INPUT file! Maybe check if .csv file contains redundant 'comma' lines.");
			System.exit(-1);
		}
		
		//convert ArrayList to String []:
		String [] a = new String [reactor_inputs.size()];
		for (int i = 0; i < reactor_inputs.size(); i++){
			a[i] = reactor_inputs.get(i);
		} 
		return a;
	}
	/**
	 * check if number of parameters to be fitted, given in INPUT.txt is equal to the number of ones given in the optimization section in INPUT.txt
	 * @param fix_reactions
	 * @param no_params
	 * @return
	 */
	public static boolean check_no_parameters(int [][] fix_reactions, int no_params){
		int counter = 0;
		for (int i = 0; i < fix_reactions.length; i++){
			for (int j = 0; j < fix_reactions[0].length; j++){
				counter+=fix_reactions[i][j];
			}
		}
		if (counter == no_params) return true;
		else return false;
	}


}
