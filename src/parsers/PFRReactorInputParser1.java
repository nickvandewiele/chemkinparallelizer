package parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import parameter_estimation.ParameterEstimationDriver;
import readers.PFRReactorInput;
import readers.ReactorInput;

public class PFRReactorInputParser1 implements ReactorInputParsable {

	String workingDir;
	String reactor_input_database_filename;
	
	List<ReactorInput> reactorInputs;

	String[] dummy;

	double total_molflrt = 0.0;

	int position_T_profile;
	List<Double> axialPositionTprofile;

	int position_P_profile;
	List<Double> axialPositionPprofile;

	public PFRReactorInputParser1(String workingDir, String string) {
		this.workingDir = workingDir;
		this.reactor_input_database_filename = string;
	}

	public void read() {
		double total_massflrt = 0.0;

		reactorInputs = new ArrayList<ReactorInput>();

		BufferedReader in_excel = null;
		try {
			in_excel = new BufferedReader(new FileReader(reactor_input_database_filename));

		} catch (FileNotFoundException e1) {}

		//read first line of excel input:
		/*
		 * Reading the first line will reveal:<BR>
		 * <LI>the number and names of species at the reactor inlet</LI>
		 * <LI>the T profile</LI>
		 * <LI>the P profile</LI>		
		 */

		String[] reactor_dim;
		try{
			reactor_dim = in_excel.readLine().split(",");

			double convert_m_cm = 100;
			double length = Double.parseDouble(reactor_dim[1])*convert_m_cm;
			double diameter = Double.parseDouble(reactor_dim[2])*convert_m_cm;

			dummy = in_excel.readLine().split(",");

			//NOS : number of species
			int NOS = dummy.length-1;
			List<String> species_name = new ArrayList<String>();
			for (int i = 0; i < NOS; i++){
				species_name.add(dummy[1+i]);
			}

			dummy = in_excel.readLine().split(",");
			List<Double> species_mw = new ArrayList<Double>();
			for (int i = 0; i < NOS; i++){
				species_mw.add(Double.parseDouble(dummy[1+i]));
			}

			String [] exp = in_excel.readLine().split(",");

			int counter=0;
			while(!exp[counter].equals("TEMP")){
				counter++;
			}
			position_T_profile = counter;
			counter++;

			//start reading the axial positions of the Temperature Profile:
			axialPositionTprofile = new ArrayList<Double>();
			while(!exp[counter].equals("PRESS")){
				axialPositionTprofile.add(Double.parseDouble(exp[counter])*convert_m_cm);
				counter++;
			}
			position_P_profile = counter;
			counter++;

			//start reading the axial positions of the Pressure Profile:
			axialPositionPprofile = new ArrayList<Double>();
			while(!exp[counter].equals("ATOL")){
				axialPositionPprofile.add(Double.parseDouble(exp[counter])*convert_m_cm);
				counter++;
			}

			//start reading in the data for each experiment:
			int experiment_counter = 0;
			String line = null;

			line = in_excel.readLine();
			while(!(line == null)){

				PFRReactorInput input = new PFRReactorInput(ReactorInput.PFR,workingDir);
				input.length = length;
				input.diameter = diameter;
				input.NOS = NOS;
				input.species_name = species_name;
				input.species_mw = species_mw;

				String [] dummy_array = line.split(",");
				//experiment_counter contains the experiment number that will be used in the reactor input file name:
				experiment_counter = Integer.parseInt(dummy_array[0]);
				input.filename = "reactor_input_"+experiment_counter+".inp";

				//total mass flow rate:
				for(int i = 0 ; i < NOS; i++){
					total_massflrt = total_massflrt + Double.parseDouble(dummy_array[1+i])/3600;
					total_molflrt = total_molflrt + Double.parseDouble(dummy_array[1+i])/species_mw.get(i);
				}
				input.total_massflrt = total_massflrt;

				//Pressure Profile:
				double pressure = 0.0;
				double convert_bar_atm = 1.01325;
				for (int i = 0; i < axialPositionPprofile.size(); i++){
					pressure = Double.parseDouble(dummy_array[position_P_profile+i+1])/convert_bar_atm;
					input.p_profile.put(axialPositionPprofile.get(i), pressure);
				}

				//Temperature Profile:
				double temperature = 0.0;
				double convert_C_K = 273.15;
				for (int i = 0; i < axialPositionTprofile.size(); i++){
					temperature = Double.parseDouble(dummy_array[position_T_profile+i+1])+convert_C_K;
					input.t_profile.put(axialPositionTprofile.get(i), temperature);
				}

				//ATOL RTOL:
				input.atol = Double.parseDouble(dummy_array[position_P_profile+axialPositionPprofile.size()+1]);
				input.rtol = Double.parseDouble(dummy_array[position_P_profile+axialPositionPprofile.size()+2]);

				//Inlet Species:
				double molfr = 0.0;
				for(int i = 0 ; i < NOS; i++){
					molfr = (Double.parseDouble(dummy_array[1+i])/species_mw.get(i))/total_molflrt;
					input.species.put(species_name.get(i), molfr);
				}

				reactorInputs.add(input);

				line = in_excel.readLine();
			}
		} catch (IOException e1) {}
	}

	/**
	 * Start writing the actual reactor input file, by doing the following:<BR>
	 * <LI>read in the lines of the template reactor input file that remain unchanged. write them to your output file</LI>
	 * <LI>change total mass flow rate</LI>
	 * <LI>add Pressure profile</LI>
	 * <LI>add Temperature profile</LI>
	 * <LI>add diameter</LI>
	 * <LI>add ATOL RTOL</LI>
	 */
	public void write() {
		Iterator<ReactorInput> iter = reactorInputs.iterator();
		while(iter.hasNext()){
			PFRReactorInput input = (PFRReactorInput)iter.next();//cast to access PFR reactor input attributes 
			try {
				PrintWriter out = new PrintWriter(new FileWriter(new File(this.workingDir,input.filename)));

				out.println("PLUG   ! Plug Flow Reactor"+
						"\nXSTR 0.0   ! Starting Axial Position (cm)");

				out.println("FLRT"+" "+input.total_massflrt);

				//Write Pressure Profile:
				for (int i = 0; i < axialPositionPprofile.size(); i++){
					out.println("PPRO "+axialPositionPprofile.get(i)+" "+input.p_profile.get(axialPositionPprofile.get(i)));
				}
				//Temperature Profile:
				for (int i = 0; i < axialPositionTprofile.size(); i++){
					out.println("TPRO "+axialPositionTprofile.get(i)+" "+input.t_profile.get(axialPositionTprofile.get(i)));
				}

				//Diameter:
				out.println("DIAM "+input.diameter);

				//ATOL RTOL:
				out.println("ATOL " + input.atol);
				out.println("RTOL " + input.rtol);

				//reactor length: 
				out.println("XEND "+input.length);

				for(int i = 0 ; i < input.NOS; i++){
					out.println("REAC "+input.species_name.get(i)+" "+input.species.get(input.species_name.get(i)));
				}

				//force solver to use nonnegative species fractions:
				out.println("NNEG");

				//END:
				out.println("END");

				out.close();
			}catch (IOException e){}
		}
	}
	public List<ReactorInput> parse() {
		read();

		write();

		return reactorInputs;
	}


}
