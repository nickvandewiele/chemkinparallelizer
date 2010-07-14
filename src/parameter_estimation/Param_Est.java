package parameter_estimation;
import java.util.*;
import java.io.*;


public class Param_Est extends Paths{

	/**
	 * @throws InterruptedException 
	 */
	
	public int maxeval;
	
	public int noExp;
	public String expName;
	private List<Map<String, Double>> exp;
	private List<Map<String,Double>> model;
	
	public double [][] beta;
	public double [][] betamin;
	public double [][] betamax;
	public int [][] fixReactions;
	
	//optimization flags:
	public boolean flagRosenbrock;
	public boolean flagLM;
	

	// constructor used for checking the validity of chemistry input file:
	public Param_Est(String wd, String cd, String c_inp, String [] r_inp, int no_lic){
		super(wd,cd, c_inp, r_inp, no_lic);
	}
	//construct for parity mode:
	public Param_Est(String wd, String cd, String c_inp, String [] reac_inp, int no_lic, int no_experiments, String experiments_name, double [][] bmin, double [][] bmax, int m_eval){
		this( wd,  cd, c_inp, reac_inp, no_lic);
		noExp = no_experiments;
		expName = experiments_name;
		betamin = bmin;
		betamax = bmax;
		maxeval = m_eval;
	}
	
	//constructor used for parameter optimization option:
	public Param_Est(String wd, String cd, String c_inp, String [] reac_inp, int no_lic, int no_experiments, String experiments_name, double [][] bmin, double [][] bmax, int m_eval, int [][] f_rxns, boolean flag_Rosenbrock, boolean flag_LM){
		this( wd,  cd, c_inp, reac_inp, no_lic, no_experiments, experiments_name, bmin, bmax, m_eval);
		fixReactions = f_rxns;
		this.flagRosenbrock = flag_Rosenbrock;
		this.flagLM = flag_LM;
	}
/**
 * optimizeParameters is the method that will optimize the kinetic parameters. It does so by:<BR>
 * <LI>checking the validity of the chemistry input file</LI>
 * <LI>taking the initial guesses of the kinetic parameters from the chemistry input file</LI>
 * <LI>reading the experimental database and store these values for the response variables</LI>
 * <LI>calling the actual optimization routine, i.e. the Rosenbrock algorithm</LI>
 * <LI>writing the optimized kinetic parameters to a params.txt file</LI>	
 * @throws Exception 
 */
	public void optimizeParameters() throws Exception{
		long time = System.currentTimeMillis();
		
		//check if initial input file is error-free:
		Runtime r = Runtime.getRuntime();
		CKEmulation c = new CKEmulation(workingDir, chemkinDir, outputDir, r, chem_inp);
		c.checkChemInput();
		
		// take initial guesses from chem.inp file:
		beta = initialGuess();
		System.out.println("Initial Guesses of parameters are:");
		print(beta);
		
		//read experimental data:
		List<Map<String,Double>> exp = new ArrayList<Map<String,Double>>();
		exp = experimentsParser();
		//System.out.println(exp.toString());

		Optimization optimization = new Optimization(workingDir, chemkinDir, maxeval, beta, reactorInputs, noLicenses, chem_inp, betamin, betamax, fixReactions, flagRosenbrock, flagLM, exp);
		
		//call optimization routine:
		beta = optimization.optimize(exp);
		
		//print optimized parameters:
		System.out.println("New values of parameters are: ");
		print(beta);
		PrintWriter out = new PrintWriter(new FileWriter("params.txt"));

		for (int i = 0; i < beta.length; i++) {
			out.println("Reaction "+i+": ");
			for (int j = 0; j < beta[0].length; j++){
				out.print(beta[i][j]+", ");
			}
			out.println();			
		}
		
		out.println();
		out.close();
		moveFile(outputDir, "params.txt");
		
		long timeTook = (System.currentTimeMillis() - time)/1000;
	    System.out.println("Time needed for this optimization to finish: (sec) "+timeTook);

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
			System.out.println(dummy);
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
	 * experiments_parser preprocesses the experimental data file into a easy-to-use format List<Map><String,Double>
	 * It reads the experimental data file. The format of this file (.csv) ought to be as follows:
	 * 	-first line states all the variable names, separated by commas
	 * 	-each of the following lines contain the experimental response variables, in the same order as the variable
	 *   names were declared
	 *  Each experiment is stored in a List with HashMaps as elements (see further)
	 *  The routines cuts each line in pieces, using the comma as separator
	 *  For each experiment, the variable name and the molar flow rate of each variable are put in a HashMap	 *   
	 * @return List of the experiments, with molar flowrates of the response variables
	 * @throws IOException
	 */
	public List<Map<String, Double>> experimentsParser ()throws IOException{
		exp = new ArrayList<Map<String, Double>>();
		try {
			//read experimental data file:
			BufferedReader in = new BufferedReader (new FileReader(expName));
			//read in species names on first line:
			String species_names = in.readLine();
			//System.out.println(species_names);
			String[] st_species = species_names.split(",");
			String dummy = in.readLine();
			//System.out.println(dummy);
			while(dummy!=null){
				String[] st_dummy = dummy.split(",");
				HashMap <String, Double> exp_molrates = new HashMap <String, Double>();
				for (int j = 0; j < st_species.length; j++) {
					exp_molrates.put(st_species[j],Double.parseDouble(st_dummy[j]));	
				}
				exp.add(exp_molrates);
				//System.out.println(l.toString());
				dummy = in.readLine();
				
			}
			if (exp.size()!= noExp){
				System.out.println("Experimental Database a different number of experiments than specified in INPUT file! Maybe check if .csv is created with redundand commas at the end...");
				System.exit(-1);
			}
			in.close();
		} catch(IOException e){
			System.out.println("Something went wrong during the preprocessing of the experimental data file!");
			System.exit(-1);
		}
		if((exp.size()==noExp)){
			return exp;
		}
		else{
			System.out.println("Experiments database contains different no. of experiments as defined in main class!");
			System.exit(-1);
			return null;	
		}
		
	}
	/**
	 * initial_guess returns the initial parameter guesses, found in the chem.inp file.
	 * It does so by reading the file, searching the key-String "REACTIONS	KJOULES/MOLE	MOLES"
	 * from that point on, every line is read and the 2nd and 4th subString is taken and stored in a List l
	 * The 2nd and 4th element correspond to A and Ea of the modified Arrhenius equation
	 * The List l is then converted to a double array and returned
	 * @return initial guesses for kinetic parameters, as double array 
	 * @throws IOException
	 */
	public double[][] initialGuess () throws IOException{
		
		double[][] beta = new double[fixReactions.length][fixReactions[0].length];
		try {
			BufferedReader in = new BufferedReader(new FileReader(workingDir+chem_inp));
			String dummy = in.readLine();
			
			//skip part of chem.inp about Elements, Species, Thermo
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
	
/*
 * 			A new approach is taken, providing guidelines to the user to adapt his/her reaction mechanism file according to 
 * 			what is specified in the guidelines:
 * 			GUIDELINES:
 * 			-use <=> to denote a reversible reaction (not =)
 * 			-separate kinetic parameters by a single space
 * 			-use a single space after the definition of the elementary reaction, e.g. A+B<=>C+D 1e13 0.0 200.0		
 */
			for (int i = 0; i < fixReactions.length; i++){
				dummy = in.readLine();
				String[] st_dummy = dummy.split("\\s");
				for (int j = 0; j < fixReactions[i].length; j++){
					//start with element at position 1 (not 0), because arrhenius parameters start at position 1!
					beta[i][j] = Double.parseDouble(st_dummy[j+1]);
				}
			}
			in.close();
			
		} catch (IOException e) {
			System.out.println("Problem with obtaining initial parameter guesses!");
			System.exit(-1);
		}
		return beta;
	}
	
	//has become obsolete with the current functionalities 
	public List<Map<String,Double>> getModelPredictions (){
		List<Map<String,Double>> model;
		boolean flag_CKSolnList = true;
		CKPackager ckp = new CKPackager(workingDir, chemkinDir, chem_inp, reactorInputs, noLicenses, flag_CKSolnList);
		model = ckp.getModelValues();
		return model;
	}
	/**
	 * getModelPredictions_massfrac return a list with mass fractions as 'model values' instead of molar flowrates
	 * this becomes handy for parity plots, because experimental data will often be available in mass fractions, not molar flow rates
	 * @return
	 */
	public List<Map<String,Double>> getModelPredictionsMassfrac(){
		boolean flag_CKSolnList = true;
		boolean flag_massfrac = true;
		CKPackager ckp = new CKPackager(workingDir, chemkinDir, chem_inp, reactorInputs, noLicenses, flag_CKSolnList, flag_massfrac);
		model = ckp.getModelValues();
		return model;
	}
	/**
	 * this routine produces model predictions without comparing them to experimental data
	 * @throws Exception 
	 */
	public void getExcelFiles() throws Exception{
		long time = System.currentTimeMillis();
		//check if initial input file is error-free:
		Runtime r = Runtime.getRuntime();
		CKEmulation c = new CKEmulation(workingDir, chemkinDir, outputDir, r, chem_inp);
		c.checkChemInput();
		c.join();
			
		boolean flag_CKSolnList = true;
		boolean flag_toExcel = true;
		boolean flag_massfrac = true;
		CKPackager ckp = new CKPackager(workingDir, chemkinDir, chem_inp, reactorInputs, noLicenses, flag_CKSolnList, flag_toExcel, flag_massfrac);
		ckp.getModelValues();
		moveOutputFiles();
		long timeTook = (System.currentTimeMillis() - time)/1000;
	    System.out.println("Time needed for Excel Postprocessing mode to finish: (sec) "+timeTook);
	}

	public void getParity() throws IOException, Exception{
		long time = System.currentTimeMillis();
		
		//check if initial input file is error-free:
		Runtime r = Runtime.getRuntime();
		CKEmulation c = new CKEmulation(workingDir, chemkinDir, outputDir, r, chem_inp);
		c.checkChemInput();
		c.join();
		
		List<Map<String,Double>> model = getModelPredictionsMassfrac();
		List<Map<String,Double>> exp = experimentsParser();
		List<String> speciesNames = c.getSpeciesNames();

		//WRITE PARITY FILE:
		PrintWriter out = new PrintWriter(new FileWriter(workingDir+"parity.csv"));
		
		// loop through all species:
		for(int i=0;i<speciesNames.size();i++){
			out.println(speciesNames.get(i).toString());
			// loop through all experiments:
			for(int j=0;j<exp.size();j++){
				Double experiment_value = exp.get(j).get(speciesNames.get(i));
				Double model_value = model.get(j).get(speciesNames.get(i));
				//out.println(speciesNames.get(i));
				out.println("experiment no. "+j+","+experiment_value+","+model_value+","+experiment_value);
			}
			out.println();
		}
		out.close();
		moveOutputFiles();
		moveFile(outputDir, "parity.csv");
		long timeTook = (System.currentTimeMillis() - time)/1000;
	    System.out.println("Time needed for Parity Mode to finish: (sec) "+timeTook);
	}
	
	public void print(double [][] d){
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++){
				System.out.print(d[i][j]+" ");		
			}
		}
		System.out.println();
	}
	public List<Map<String, Double>> getExp() {
		return exp;
	}
	public List<Map<String, Double>> getModel() {
		return model;
	}
	public void getStatistics() throws Exception{
		long time = System.currentTimeMillis();
		
		//check if initial input file is error-free:
		Runtime r = Runtime.getRuntime();
		CKEmulation c = new CKEmulation(workingDir, chemkinDir, outputDir, r, chem_inp);
		c.checkChemInput();
		
		// take initial guesses from chem.inp file:
		beta = initialGuess();
		System.out.println("Initial Guesses of parameters are:");
		print(beta);
		
		//read experimental data:
		List<Map<String,Double>> exp = new ArrayList<Map<String,Double>>();
		exp = experimentsParser();
		//System.out.println(exp.toString());

		Optimization optimization = new Optimization(workingDir, chemkinDir, maxeval, beta, reactorInputs, noLicenses, chem_inp, betamin, betamax, fixReactions, flagRosenbrock, flagLM, exp);
		
		optimization.calcStatistics();
		//moveOutputFiles();
		long timeTook = (System.currentTimeMillis() - time)/1000;
	    System.out.println("Time needed for this optimization to finish: (sec) "+timeTook);	    	    
	}
	
}
