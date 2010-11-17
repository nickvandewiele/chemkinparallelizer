package parameter_estimation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

public class ParameterEstimationDriver {
	public static Logger logger = Logger.getLogger(ParameterEstimationDriver.class);
	//this comment is added to verify version control system
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		long time = System.currentTimeMillis();
		initializeLog();
		//input file will be searched in working directory under the name INPUT.txt:
		BufferedReader in = new BufferedReader(new FileReader(System.getProperty("user.dir")+"/INPUT.txt"));
		
		
		in.readLine();
		String workingDir = in.readLine();
		int wdlength = workingDir.length();
		if (workingDir.charAt(wdlength-1)!='/'){
			logger.debug("Pathname to working directory needs to end with forward slash '/' !");
			System.exit(-1);
		}
		
		in.readLine();
		String chemkinDir = in.readLine();
		
		in.readLine();
		int noLicenses = Integer.parseInt(in.readLine());
		
		in.readLine();
		String chemInp = in.readLine();
		
		in.readLine();
		String experimentsDb = in.readLine();
		
		in.readLine();
		boolean flagReactorDb = Boolean.parseBoolean(in.readLine());
		if (flagReactorDb){
			if(!(new File("reactor_input_template.inp").exists())){
				logger.debug("reactor_input_template.inp was not found in the working directory!");
				System.exit(-1);
			}
		}
		
		in.readLine();
		String reactorSetupsDb = in.readLine();
		
		in.readLine();
		int noExperiments = Integer.parseInt(in.readLine());
		
		//number of parameters to be fitted:
		in.readLine();
		int noParams = Integer.parseInt(in.readLine());
		
		//optimization flags:
		in.readLine();
		boolean flagRosenbrock = Boolean.parseBoolean(in.readLine());
		in.readLine();
		boolean flagLM = Boolean.parseBoolean(in.readLine());
		
		in.readLine();
		int maxeval = Integer.parseInt(in.readLine());
		
		in.readLine();
		int mode = Integer.parseInt(in.readLine());

		//REACTOR INPUT FILE NAMES:
		in.readLine();
		
		String [] reactor_inputs = new String[noExperiments];
		if (flagReactorDb){
			in.readLine();
		}
		else {
			for (int i = 0; i < noExperiments; i++){
				reactor_inputs[i] = in.readLine(); 
			}
		
		}
	
		
		//OPTIMIZATION SECTION: modified Arrhenius parameters [A, n, Ea]: 1 = loose, parameter will be varied; 0 = fixed, parameter will be fixed
		in.readLine();
		
		//number of reactions that will be optimized:
		in.readLine();
		int noFittedReactions = Integer.parseInt(in.readLine());
		
		//number of parameters per reaction (modified Arrhenius [A,n,Ea]): 3
		int noParametersPerReaction = 3;
		
		int [][] fixRxns = new int [noFittedReactions][noParametersPerReaction];
		for (int i = 0; i < noFittedReactions; i++){
			//comment line with "reaction i: "
			in.readLine();
			// string of 1,0,1:
			String [] s = in.readLine().split(",");
			for (int j = 0; j < noParametersPerReaction; j++){
				fixRxns[i][j] = Integer.parseInt(s[j]);
			}
		}
		boolean flagNoParameters = checkNoParameters(fixRxns, noParams);
		if (!flagNoParameters) {
			logger.debug("Number of parameters to be fitted provided in INPUT.txt does not equal the number of ones you specified in the optimization section in INPUT.txt!");
			System.exit(-1);
		}
		else {
			//do nothing, continue
		}
		
		in.close();
		
		String template = "reactor_input_template.inp";
		
		double [][] betaMin = new double [noFittedReactions][noParametersPerReaction];
		double [][] betaMax = new double [noFittedReactions][noParametersPerReaction];
		for (int i = 0; i < noFittedReactions; i++) {
			for (int j = 0; j < noParametersPerReaction; j++){
				betaMin[i][j]=0;
				betaMax[i][j]=1e20;
			}
		}
		
		if (flagReactorDb){
			reactor_inputs = reactorInputsParser(workingDir, reactorSetupsDb, template, noExperiments);
		}

		Paths paths = new Paths(workingDir,chemkinDir,chemInp,reactor_inputs,noLicenses);
		Parameters2D params = new Parameters2D(null, betaMin, betaMax, fixRxns);
		switch(mode){
			case 0:	logger.info("PARITY PLOT MODE");	
					Param_Est p0 = new Param_Est(paths, params, noExperiments, experimentsDb, maxeval);
					p0.parity();
					Function f = new Function(p0.getModel(),p0.getExp());
					logger.info("SSQ is: "+f.getSRES());
					break;
			case 1:	logger.info("PARAMETER OPTIMIZATION MODE");		
					Param_Est p1 = new Param_Est(paths, params, noExperiments, experimentsDb, maxeval, flagRosenbrock, flagLM);
					p1.optimizeParameters();
					p1.statistics();
					p1.parity();
					break;
			case 2: logger.info("EXCEL POSTPROCESSING MODE");
					Param_Est p2 = new Param_Est(paths);
					p2.excelFiles();
					break;
			case 3: logger.info("STATISTICS MODE");
					Param_Est p3 = new Param_Est(paths, params, noExperiments, experimentsDb, maxeval, flagRosenbrock, flagLM);
					p3.statistics();
					p3.parity();
		}
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this program to finish: (sec) "+timeTook);
	}
	public static String[] reactorInputsParser(String workingDir, String experiments, String template, int no_experiments) throws IOException{
		ArrayList<String> reactorInputs = new ArrayList<String>();
		
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
				reactorInputs.add(filename);
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
		if( reactorInputs.size()!= no_experiments){
			logger.debug("Number of experiments in reactor inputs file does not correspond to the number of experiments provided in the INPUT file! Maybe check if .csv file contains redundant 'comma' lines.");
			System.exit(-1);
		}
		
		//convert ArrayList to String []:
		String [] a = new String [reactorInputs.size()];
		for (int i = 0; i < reactorInputs.size(); i++){
			a[i] = reactorInputs.get(i);
		} 
		return a;
	}
	/**
	 * check if number of parameters to be fitted, given in INPUT.txt is equal to the number of ones given in the optimization section in INPUT.txt
	 * @param fix_reactions
	 * @param no_params
	 * @return
	 */
	public static boolean checkNoParameters(int [][] fix_reactions, int no_params){
		int counter = 0;
		for (int i = 0; i < fix_reactions.length; i++){
			for (int j = 0; j < fix_reactions[0].length; j++){
				counter+=fix_reactions[i][j];
			}
		}
		return (counter == no_params); 
		
	}
	public static void initializeLog(){
		SimpleLayout layout = new SimpleLayout();
		
		//make Appender, it's a FileAppender, writing to loggerNick.txt:
		FileAppender appender = null;
		try {
			appender = new FileAppender(layout, "NBMT.log", false);
		} catch(Exception e) {}
		
		//add Appender:
		logger.addAppender(appender);

//		BasicConfigurator.configure();
	}

}
