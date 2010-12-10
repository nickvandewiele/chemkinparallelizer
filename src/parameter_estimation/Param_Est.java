package parameter_estimation;
import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;


public class Param_Est{
	static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
	private Paths paths;
	private Chemistry chemistry;
	private Experiments experiments;
	private Fitting fitting;
	private Licenses licenses;
	private List<String> speciesNames;

	/**
	 * @category getter
	 * @return
	 */
	public Paths getPaths() {
		return paths;
	}
	private ModelValues modelValues;
	public void setModelValues(ModelValues modelValues) {
		this.modelValues = modelValues;
	}
	// constructor used for checking the validity of chemistry input file:
	public Param_Est(Paths paths, Chemistry chemistry, Experiments experiments,
			Licenses licenses){
		this.paths = paths;
		this.chemistry = chemistry;
		this.experiments = experiments;
		this.licenses = licenses;
		/**
		 * create reactor input files, if necessary:
		 */
		if(experiments.isFlagReactorDB()){
			experiments.setRegularReactorInputs(createRegularReactorInputs());
		}
		
		//TODO should check whether merge is successful
		//fill ReactorInputs with RegularReactorInputs, IgnitionDelayInputs:
		experiments.mergeReactorInputs();
		
		//set flags for ignition delays:
		experiments.setFlagIgnitionDelays();
	}
	//construct for parity mode:

	//constructor used for parameter optimization option:

	public Param_Est(Paths paths, Chemistry chemistry,
			Experiments experiments, Fitting fitting, Licenses licenses) {
		this.paths = paths;
		this.chemistry = chemistry;
		this.experiments = experiments;
		this.fitting = fitting;
		this.licenses = licenses;

		/**
		 * create reactor input files, if necessary:
		 */
		if(experiments.isFlagReactorDB()){
			experiments.setRegularReactorInputs(createRegularReactorInputs());
		}
		
		//TODO should check whether merge is successful
		//fill ReactorInputs with RegularReactorInputs, IgnitionDelayInputs:
		experiments.mergeReactorInputs();
		
		//set flags for ignition delays:
		experiments.setFlagIgnitionDelays();
	}
	/**
	 * optimizeParameters is the method that will optimize the kinetic parameters. It does so by:<BR>
	 * <LI>checking the validity of the chemistry input file</LI>
	 * <LI>taking the initial guesses of the kinetic parameters from the chemistry input file</LI>
	 * <LI>reading the experimental database and store these values for the response variables</LI>
	 * <LI>calling the actual optimization routine, i.e. the Rosenbrock algorithm</LI>
	 * <LI>writing the optimized kinetic parameters to a params.txt file</LI>	
	 * @throws Exception 
	 * @throws Exception 
	 */
	public void optimizeParameters() throws Exception{
		long time = System.currentTimeMillis();

		//check if initial input file is error-free:
		Runtime r = Runtime.getRuntime();
		CKEmulation c = new CKEmulation(paths, chemistry, r);
		c.checkChemOutput();

		// take initial guesses from chem.inp file:
		chemistry.getParams().setBeta(Chemistry.initialGuess(paths.getWorkingDir(), 
				chemistry.getChemistryInput(),
				chemistry.getParams().getFixRxns()));
		logger.info("Initial Guesses of parameters are:");
		//Printer.printMatrix(chemistry.getParams().getBeta(),System.out);

		//read experimental data:
		readExperimentalData();

		Optimization optimization = new Optimization(paths, chemistry, experiments, fitting, licenses);

		//call optimization routine:
		chemistry.getParams().setBeta(optimization.optimize());

		//write optimized parameters:
		writeParameters();
		Tools.moveFile(paths.getOutputDir(), "params.txt");

		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this optimization to finish: (sec) "+timeTook);

	}
	/**	
	 * plug new parameter guesses into the chemkin chemistry input file (usually chem.inp)
	 * write new update chem.inp file
	 * return the chemistry input filename
	 * WARNING: method supposes a pre-conditioned chem.inp file, processed by ChemClean and with TD inside chem.inp!!!
	 */
	public static String updateChemistryInput (String wd, double [] beta_new) throws IOException{
		String chemistry_input="chem.inp";
		String path_old_chem = wd+chemistry_input;	

		BufferedReader in = new BufferedReader(new FileReader(path_old_chem));
		PrintWriter out = new PrintWriter(new FileWriter(wd+"temp.inp"));
		String dummy = in.readLine();
		//just copy part of chem.inp about Elements, Species, Thermo
		boolean b = true;
		while(b){
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
		int i = 0;
		out.println(dummy);

		//read in next line, otherwise "REACTIONS" is still present in dummy String
		dummy = in.readLine();

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
			logger.info(dummy);
			out.println(dummy);
			dummy = in.readLine();
		}
		// write "END" line:
		out.println(dummy);

		in.close();
		out.close();

		//delete old chem.inp file and create new chem.inp based on temp.inp:
		File f_old = new File(path_old_chem);
		f_old.delete();
		File f = new File(wd+"temp.inp");
		f.renameTo(new File(path_old_chem));
		return chemistry_input;
	}

	/**
	 * this routine produces model predictions without comparing them to experimental data
	 * @throws Exception 
	 * @throws Exception 
	 */
	public void excelFiles() throws Exception{
		long time = System.currentTimeMillis();
		//check if initial input file is error-free:
		Runtime r = Runtime.getRuntime();
		CKEmulation c = new CKEmulation(paths, chemistry, r);
		c.checkChemOutput();
		c.join();

		boolean flag_CKSolnList = true;
		boolean flag_toExcel = true;
		CKPackager ckp = new CKPackager(paths, chemistry,
				experiments, licenses, flag_CKSolnList, flag_toExcel);

		moveOutputFiles();
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for Excel Postprocessing mode to finish: (sec) "+timeTook);
	}
	//TODO parity should be type, not method, i believe
	public void parity() throws Exception{
		long time = System.currentTimeMillis();

		//check if initial input file is error-free:
		Runtime r = Runtime.getRuntime();
		CKEmulation c = new CKEmulation(paths, chemistry, r);
		c.checkChemOutput();
		c.join();

		boolean flag_CKSolnList = true;
		CKPackager ckp = new CKPackager(paths, chemistry, experiments, licenses,
				flag_CKSolnList);
		
		
		modelValues = ckp.getModelValues();
		
		readExperimentalData();
		
		speciesNames = Tools.getSpeciesNames(c.getPaths().getWorkingDir(), c.getAsu());

		//WRITE PARITY FILE:
		PrintWriter out = new PrintWriter(new FileWriter(paths.getWorkingDir()+"SpeciesParity.csv"));
		writeSpeciesParities(out,speciesNames);
		out.close();
		out = new PrintWriter(new FileWriter(paths.getWorkingDir()+"IgnitionDelayParity.csv"));
		writeIgnitionDelayParities(out);
		out.close();

		moveOutputFiles();
		//TODO moveFiles should be done less patchy
		Tools.moveFile(paths.getOutputDir(), "SpeciesParity.csv");
		Tools.moveFile(paths.getOutputDir(), "IgnitionDelayParity.csv");
		
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for Parity Mode to finish: (sec) "+timeTook);
	}

	/**
	 * @category 
	 * @return
	 * @throws IOException
	 */
	public ModelValues getModelValues(CKPackager ckp) {
		//check whether model values have already been stored:
		try {
			modelValues = ckp.getModelValues();
		} catch (Exception e) {
			logger.debug(e);
		}
		return modelValues;

	}
	public void statistics() throws Exception{
		long time = System.currentTimeMillis();

		//check if initial input file is error-free:
		Runtime r = Runtime.getRuntime();
		CKEmulation c = new CKEmulation(paths, chemistry, r);
		c.checkChemOutput();

		// take initial guesses from chem.inp file:
		chemistry.getParams().setBeta(Chemistry.initialGuess(paths.getWorkingDir(),
				chemistry.getChemistryInput(),
				chemistry.getParams().getFixRxns()));

		//read experimental data:
		readExperimentalData();

		Optimization optimization = new Optimization(paths, chemistry, experiments, fitting, licenses);

		optimization.calcStatistics();
		//moveOutputFiles();
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this optimization to finish: (sec) "+timeTook);	    	    
	}
	protected void moveOutputFiles (){
		Tools.moveFiles(paths.getWorkingDir(), paths.getOutputDir(), ".out");
		Tools.moveFiles(paths.getWorkingDir(), paths.getOutputDir(), ".asu");
		Tools.moveFiles(paths.getWorkingDir(), paths.getOutputDir(), ".input");
		Tools.moveFiles(paths.getWorkingDir(), paths.getOutputDir(), ".asc");
		Tools.moveFile(paths.getOutputDir(),"CKSolnList.txt");

	}
	/**
	 * standard reactor input parser where T, P profiles can be specified, but with
	 * only 1 reactor length specified
	 * @param workingDir
	 * @return
	 * @throws IOException
	 */
	public LinkedList<String> reactorInputsParser() throws IOException{
		LinkedList<String> reactorInputs = new LinkedList<String>();

		//read first line of excel input:
		/*
		 * Reading the first line will reveal:<BR>
		 * <LI>the number and names of species at the reactor inlet</LI>
		 * <LI>the T profile</LI>
		 * <LI>the P profile</LI>		
		 */
		BufferedReader in_excel = new BufferedReader(new FileReader(paths.getWorkingDir()+experiments.getReactorSetupDB()));
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
			while(!(dummy == null)){				
				BufferedReader in_template = new BufferedReader(new FileReader(experiments.getPathPFRTemplate()));
				String [] dummy_array = line.split(",");
				//experiment_counter contains the experiment number that will be used in the reactor input file name:
				experiment_counter = Integer.parseInt(dummy_array[0]);
				String filename = "reactor_input_"+experiment_counter+".inp";
				reactorInputs.add(filename);
				PrintWriter out = new PrintWriter(new FileWriter(paths.getWorkingDir()+filename));

				//copy the first 2 lines:
				for(int i = 0 ; i < 2 ; i++){
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
		}catch (Exception e){
			logger.debug("Something went wrong in reactorInputParser",e);
		}//do nothing: e catches the end of the file exception

		// verify the correct number of lines in reactor input file:
		if( reactorInputs.size()!= experiments.getNoRegularExperiments()){
			ParameterEstimationDriver.logger.debug("Number of experiments in reactor inputs file does not correspond to the number of experiments provided in the INPUT file! Maybe check if .csv file contains redundant 'comma' lines.");			
			System.exit(-1);
		}

		return reactorInputs;
	}

	/**
	 * this parsers is designed for the reactor input with
	 * isothermal, isobaric reactor profiles, but variable reactor lengths
	 * 
	 * This becomes handy when different sets of experimental data are to be compared, and the 
	 * 'equivalent' reactor length is calculated.
	 * @param workingDir
	 * @return
	 * @throws IOException
	 */
	public LinkedList<String> reactorInputsParser2 () throws IOException{
		LinkedList<String> reactorInputs = new LinkedList<String>();
		String filename;
		//read first line of excel input:
		/*
		 * Reading the first line will reveal:<BR>
		 * <LI>the number and names of species at the reactor inlet</LI>
		 * <LI>the T profile</LI>
		 * <LI>the P profile</LI>		
		 */
		BufferedReader in_excel = new BufferedReader(new FileReader(paths.getWorkingDir()+experiments.getReactorSetupDB()));

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
		in_excel.readLine();
		try {
			line = in_excel.readLine();

			double convert_m_cm = 100;
			double convert_C_K = 273.15;
			double diameter, length, temperature, pressure;

			while(!(dummy == null)){				
				BufferedReader in_template = new BufferedReader(new FileReader(paths.getWorkingDir()+experiments.getPathCSTRTemplate()));
				String [] dummy_array = line.split(",");
				//experiment_counter contains the experiment number that will be used in the reactor input file name:
				experiment_counter = Integer.parseInt(dummy_array[0]);
				filename = "reactor_input_"+experiment_counter+".inp";

				reactorInputs.add(filename);
				PrintWriter out = new PrintWriter(new FileWriter(paths.getWorkingDir()+filename));

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

				convert_m_cm = 100;
				//Diameter:
				diameter = Double.parseDouble(dummy_array[NOS+1]) * convert_m_cm;
				out.println("DIAM "+diameter);

				//reactor length: 
				length = Double.parseDouble(dummy_array[NOS+2]) * convert_m_cm;
				out.println("XEND "+length);

				//temperature:
				temperature = Double.parseDouble(dummy_array[NOS+3])+convert_C_K;
				out.println("TPRO 0.0"+" "+temperature);
				out.println("TPRO "+length+" "+temperature);

				//pressure:
				pressure = Double.parseDouble(dummy_array[NOS+4]);
				out.println("PPRO 0.0"+" "+pressure);
				out.println("PPRO "+length+" "+pressure);

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
		}catch (Exception e){
			logger.debug("Something went wrong in reactorInputParser2",e);
		}//do nothing: e catches the end of the file exception

		// verify the correct number of lines in reactor input file:
		if( reactorInputs.size()!= experiments.getNoRegularExperiments()){
			System.out.println("Number of experiments in reactor inputs file does not correspond to the number of experiments provided in the INPUT file! Maybe check if .csv file contains redundant 'comma' lines.");
			System.exit(-1);
		}


		return reactorInputs;
	}

	public LinkedList<String> createRegularReactorInputs(){
		LinkedList<String> RegularReactorInputs = new LinkedList<String>();
		if (experiments.isFlagReactorDB()){
			if(experiments.getFlagReactorSetupType()==1){
				try {
					RegularReactorInputs = reactorInputsParser();
				} catch (IOException e) {
					logger.debug(e);
				}
			}
			if(experiments.getFlagReactorSetupType()==2){
				try {
					RegularReactorInputs = reactorInputsParser2();
				} catch (IOException e) {
					logger.debug(e);
				}
			}
		}
		return RegularReactorInputs;
	}
	public void writeSpeciesParities(PrintWriter out, List<String> speciesNames){
		// loop through all species:
		for(int i=0;i<speciesNames.size();i++){
			out.println(speciesNames.get(i).toString());
			// loop through all experiments:
			for(int j=0;j<experiments.getExperimentalValues().getExperimentalEffluentValues().size();j++){
				Double experiment_value = experiments.getExperimentalValues().getExperimentalEffluentValues().get(j).get(speciesNames.get(i));
				Double model_value = modelValues.getModelEffluentValues().get(j).get(speciesNames.get(i));
				//out.println(speciesNames.get(i));
				out.println("experiment no. "+j+","+experiment_value+","+model_value+","+experiment_value);

			}
			out.println();
		}
	}
	public void writeIgnitionDelayParities(PrintWriter out){	
			// loop through all experiments:
			for(int j=0;j<experiments.getExperimentalValues().getExperimentalIgnitionValues().size();j++){
				Double experiment_value = experiments.getExperimentalValues().getExperimentalIgnitionValues().get(j);
				Double model_value = modelValues.getModelIgnitionValues().get(j);
				//out.println(speciesNames.get(i));
				out.println("experiment no. "+j+","+experiment_value+","+model_value+","+experiment_value);

			}
			out.println();
	}
	public void readExperimentalData(){
		
		//get experimental effluent values, if they exist:
		if(!(experiments.getPathExperimentalDB().equals(""))){
			try {
				//fill in response variables:
				experiments.setResponseVariables(experiments.readResponseVariables(paths.getWorkingDir()));
				
				ExperimentalValues experimentalValues = new ExperimentalValues();
				LinkedList<Map<String,Double>> list = Tools.experimentsEffluentParser(paths.getWorkingDir()+experiments.getPathExperimentalDB(),
						experiments.getNoRegularExperiments());
				experimentalValues.setExperimentalEffluentValues(list);
				experiments.setExperimentalValues(experimentalValues);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		if(!(experiments.getPathIgnitionDB().equals(""))){
			try {
				ExperimentalValues experimentalValues;
				if(experiments.getExperimentalValues() == null){
					experimentalValues = new ExperimentalValues();
				}
				else{
					experimentalValues = experiments.getExperimentalValues();	
				}
				LinkedList<Double> list = Tools.experimentsIgnitionParser(paths.getWorkingDir()+experiments.getPathIgnitionDB(),
						experiments.getNoIgnitionDelayExperiments());
				experimentalValues.setExperimentalIgnitionValues(list);
				experiments.setExperimentalValues(experimentalValues);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}
	}
	public void writeParameters(){
		logger.info("New values of parameters are: ");
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("params.txt"));
			for (int i = 0; i < chemistry.getParams().getBeta().length; i++) {
				out.println("Reaction "+i+": ");
				for (int j = 0; j < chemistry.getParams().getBeta()[0].length; j++){
					out.print(chemistry.getParams().getBeta()[i][j]+", ");
				}
				out.println();			
			}
			out.println();
			out.close();
		} catch (IOException e) {
			logger.debug(e);
		}

		
	}
}
