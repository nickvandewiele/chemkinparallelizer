

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import parameter_estimation.Param_Est;

public class ParameterEstimationDriver_Steven {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception{
/*		String workingDir = System.getProperty("user.dir");
		workingDir +="/";
*/		
		BufferedReader in = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/INPUT.txt"));
		in.readLine();
		String workingDir = in.readLine();
		in.readLine();
		String chemkinDir = in.readLine();
		in.readLine();
		String chem_inp = in.readLine();
		in.readLine();
		String experiments = in.readLine();
		in.readLine();
		String template = "reactor_input_template.inp";
		in.close();
/*		
		String workingDir = "C:/Documents and Settings/nmvdewie/My Documents/chemkin/Parameter_Estimation_v2_multithreading/";
		String chemkinDir = "C:/Program Files/Reaction/chemkin41_pc";
		String chem_inp = "chem_naphtha1.inp";
		String experiments = "reactor_input.csv";
		String template = "reactor_input_template.inp";
*/		
		String [] reactor_inputs = reactor_inputs_parser(workingDir, experiments, template);
		Param_Est p = new Param_Est(workingDir, chemkinDir, chem_inp, reactor_inputs);
		p.getExcelFiles();
		
	}
	public static String[] reactor_inputs_parser(String workingDir, String experiments, String template) throws Exception{
		ArrayList<String> reactor_inputs = new ArrayList<String>();
		
	//read first line of excel input:
		/*
		 * Reading the first line will reveal:
		 * -the number and names of species at the reactor inlet
		 * -the T profile
		 * -the P profile		
		 */
		BufferedReader in_excel = new BufferedReader(new FileReader(workingDir+experiments));
		String [] reactor_dim = in_excel.readLine().split(",");
		double length = Double.parseDouble(reactor_dim[1]);
		double diameter = Double.parseDouble(reactor_dim[2]);
		
		String [] dummy = in_excel.readLine().split(",");
//		NOS=number of species
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
		ArrayList<String> array_Tprofile = new ArrayList<String>();
		while(!exp[counter].equals("PRESS")){
			array_Tprofile.add(exp[counter]);
			counter++;
		}
		int position_P_profile = counter;
		counter++;
		//start reading the axial positions of the Pressure Profile:
		ArrayList<String> array_Pprofile = new ArrayList<String>();
		for (int i = counter; i < exp.length; i++){
			array_Pprofile.add(exp[i]);
		}
/*
 * Start writing the actual reactor input file, by doing the following:
 * -read in the lines of the template reactor input file that remain unchanged. write them to your output file
 * -change total mass flow rate
 * -add Pressure profile
 * -add Temperature profile
 * -add diameter
 * -
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
				//copy the first 9 lines:
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
				for (int i = 0; i < array_Pprofile.size(); i++){
					pressure = Double.parseDouble(dummy_array[position_P_profile+i+1])/1.01325;
					out.println("PPRO "+array_Pprofile.get(i)+" "+pressure);
				}
				//Temperature Profile:
				double temperature = 0.0;
				for (int i = 0; i < array_Tprofile.size(); i++){
					temperature = Double.parseDouble(dummy_array[position_T_profile+i+1])+273.15;
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
				//END:
				out.println("END");
				
				in_template.close();
				out.close();
				line = in_excel.readLine();
			}
			in_excel.close();
		}catch (Exception e){}//do nothing: e catches the end of the file exception
		
		//convert ArrayList to String []:
		String [] a = new String [reactor_inputs.size()];
		for (int i = 0; i < reactor_inputs.size(); i++){
			a[i] = reactor_inputs.get(i);
		} 
		return a;
	}
}
