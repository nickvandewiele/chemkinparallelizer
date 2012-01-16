package parameter_estimation;
import java.util.*;
import java.io.*;

import org.apache.log4j.Logger;

import readers.ConfigurationInput;
import readers.ReactorInput;
import readers.ReactorInputParsable;
import readers.PFRReactorInputParser1;


public class Param_Est extends Loggable{

	ConfigurationInput config;

	private Fitting fitting;

	private LinkedList<String> speciesNames;

	/**
	 * @category getter
	 * @return
	 */

	private ModelValue[] modelValues;
	/**
	 * @category setter
	 * @param modelValues
	 */
	public void setModelValues(ModelValues modelValues) {
		this.modelValues = modelValues;
	}

	public Param_Est(ConfigurationInput config) {
		this.config = config;

		/**
		 * create reactor input files, if necessary:
		 */
		if(experiments.isFlagReactorDB()){
			experiments.getReactorInputCollector().setRegularInputs(createRegularReactorInputs());
		}

		//TODO should check whether merge is successful
		//fill ReactorInputs with RegularReactorInputs, IgnitionDelayInputs:
		experiments.getReactorInputCollector().mergeReactorInputs();

		//set flags for ignition delays:
		experiments.getReactorInputCollector().setFlagIgnitionDelays();

		//set flags for flame speeds:
		experiments.getReactorInputCollector().setFlagFlameSpeeds();
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
		checkChemistryFile(r);

		// take initial guesses from chem.inp file:
		chemistry.getParams().setBeta(Chemistry.initialGuess(paths.getWorkingDir(), 
				chemistry.getChemistryInput(),
				chemistry.getParams().getFixRxns()));
		logger.info("Initial Guesses of parameters are:");
		//Printer.printMatrix(chemistry.getParams().getBeta(),System.out);

		String workingDir = paths.getWorkingDir();
		ExperimentalValues experimentalValues = experiments.readExperimentalData(workingDir); 
		experiments.setExperimentalValues(experimentalValues);
		

		Optimization optimization = new Optimization(paths, chemistry, experiments, fitting, licenses);

		//call optimization routine:
		chemistry.getParams().setBeta(optimization.optimize());

		//write optimized parameters:
		PrintWriter out = new PrintWriter(new FileWriter("params.txt"));
		writeParameters(out);
		Tools.moveFile(paths.getOutputDir(), "params.txt");

		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for this optimization to finish: (sec) "+timeTook);

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
		checkChemistryFile(r);

		AbstractCKPackager ckp = new CKPackager(config);
		ckp.runAllSimulations();
		
		moveOutputFiles();
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for Excel Postprocessing mode to finish: (sec) "+timeTook);
	}
	private void checkChemistryFile(Runtime r) throws IOException,
	InterruptedException, FileNotFoundException {
		CKEmulation c = new CKEmulation(paths, chemistry, r);
		c.preProcess(r);
		BufferedReader in = new BufferedReader(new FileReader(paths.getWorkingDir()+ChemkinConstants.CHEMOUT));
		c.checkChemOutput(in);
		c.join();
	}
	//TODO parity should be type, not method, i believe
	public void parity() throws Exception{
		long time = System.currentTimeMillis();

		//check if initial input file is error-free:
		Runtime r = Runtime.getRuntime();
		checkChemistryFile(r);

		AbstractCKPackager ckp = new CKPackager(config);
		ckp = new ExtractModelValuesPackagerDecorator(ckp);
		ckp.runAllSimulations();
		modelValues = ckp.modelValues;

		//read experimental data file:
		String workingDir = paths.getWorkingDir();
		ExperimentalValues experimentalValues = experiments.readExperimentalData(workingDir); 
		experiments.setExperimentalValues(experimentalValues);

		String speciesPath = paths.getWorkingDir()+ChemkinConstants.CHEMASU;
		BufferedReader inSpecies = new BufferedReader (new FileReader(speciesPath));
		speciesNames = Chemistry.readSpeciesNames(inSpecies);

		//WRITE PARITY FILE:
		PrintWriter out;
		if(experiments.getReactorInputCollector().getNoRegularExperiments()!=0){
			out = new PrintWriter(new FileWriter(paths.getWorkingDir()+"SpeciesParity.csv"));
			writeSpeciesParities(out,speciesNames);
			out.close();
		}
		if(experiments.getReactorInputCollector().getNoIgnitionDelayExperiments()!=0){
			out = new PrintWriter(new FileWriter(paths.getWorkingDir()+"IgnitionDelayParity.csv"));
			writeIgnitionDelayParities(out);
			out.close();	
		}
		if(experiments.getReactorInputCollector().getNoFlameSpeedExperiments()!=0){
			out = new PrintWriter(new FileWriter(paths.getWorkingDir()+"FlameSpeedParity.csv"));
			writeFlameSpeedParities(out);
			out.close();	
		}

		moveOutputFiles();

		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for Parity Mode to finish: (sec) "+timeTook);
	}

	private void writeFlameSpeedParities(PrintWriter out) {
		out.println("Experiment: "+"\t"+"Experimental Value"+"\t"+"Model Value"+"\t"+"Experimental Value");
		// loop through all experiments:
		for(int j=0;j<experiments.getExperimentalValues().getExperimentalFlameSpeedValues().size();j++){
			Double experiment_value = experiments.getExperimentalValues().getExperimentalFlameSpeedValues().get(j);
			Double model_value = modelValues.getModelFlameSpeedValues().get(j);
			//out.println(speciesNames.get(i));
			out.println("experiment no. "+j+","+experiment_value+","+model_value+","+experiment_value);

		}
		out.println();
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
		c.preProcess(r);
		BufferedReader in = new BufferedReader(new FileReader(paths.getWorkingDir()+ChemkinConstants.CHEMOUT));
		c.checkChemOutput(in);

		// take initial guesses from chem.inp file:
		chemistry.getParams().setBeta(Chemistry.initialGuess(paths.getWorkingDir(),
				chemistry.getChemistryInput(),
				chemistry.getParams().getFixRxns()));

		//read experimental data file:
		String workingDir = paths.getWorkingDir();
		ExperimentalValues experimentalValues = experiments.readExperimentalData(workingDir); 
		experiments.setExperimentalValues(experimentalValues);

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
		Tools.moveFile(paths.getOutputDir(),"SpeciesParity.csv");
		Tools.moveFile(paths.getOutputDir(),"IgnitionDelayParity.csv");
		Tools.moveFile(paths.getOutputDir(),"FlameSpeedParity.csv");
		Tools.moveFile(paths.getOutputDir(),"CKSolnList.txt");

	}
	public LinkedList<String> createRegularReactorInputs(){
		LinkedList<String> RegularReactorInputs = new LinkedList<String>();
		ReactorInputParsable parser;
		if (experiments.isFlagReactorDB()){
				if(experiments.getFlagReactorSetupType()==1){
					try {		
						parser = new PFRReactorInputParser1(config.getWorking_dir()+experiments.getReactorSetupDB());		
						RegularReactorInputs = parser.parse();
					} catch (IOException e) {
						logger.debug(e);
					}
				}
		}
		return RegularReactorInputs;
	}
	private void writeSpeciesParities(PrintWriter out, List<String> speciesNames){
		StringBuffer stringBuff = new StringBuffer();
		stringBuff.append("Experiment: "+"\t"+"Experimental Value"+"\t"+"Model Value"+"\t"+"Experimental Value\n");
		// loop through all species:
		for(int i=0;i<speciesNames.size();i++){
			out.println(speciesNames.get(i).toString());
			// loop through all experiments:
			for(int j=0;j<experiments.getExperimentalValues().getExperimentalEffluentValues().size();j++){
				Double experiment_value = experiments.getExperimentalValues().getExperimentalEffluentValues().get(j).get(speciesNames.get(i));
				Double model_value = modelValues.getModelEffluentValues().get(j).get(speciesNames.get(i));
				//out.println(speciesNames.get(i));
				stringBuff.append("experiment no. "+j+","+experiment_value+","+model_value+","+experiment_value+"\n");
			}
			stringBuff.append("\n");
		}
		out.println(stringBuff.toString());
	}
	private void writeIgnitionDelayParities(PrintWriter out){	
		// loop through all experiments:
		StringBuffer stringBuff = new StringBuffer();
		stringBuff.append("Experiment: "+"\t"+"Experimental Value"+"\t"+"Model Value"+"\t"+"Experimental Value\n");
		for(int j=0;j<experiments.getExperimentalValues().getExperimentalIgnitionValues().size();j++){
			Double experiment_value = experiments.getExperimentalValues().getExperimentalIgnitionValues().get(j);
			Double model_value = modelValues.getModelIgnitionValues().get(j);
			//out.println(speciesNames.get(i));
			stringBuff.append("experiment no. "+j+","+experiment_value+","+model_value+","+experiment_value+"\n");

		}
		out.println(stringBuff.toString());
	}
	public void writeParameters(PrintWriter out){
		logger.info("New values of parameters are: ");
		StringBuffer stringBuff = new StringBuffer();
		for (int i = 0; i < chemistry.getParams().getBeta().length; i++) {
			stringBuff.append("Reaction "+i+": \n");
			for (int j = 0; j < chemistry.getParams().getBeta()[0].length; j++){
				stringBuff.append(chemistry.getParams().getBeta()[i][j]+", \n");
			}
			stringBuff.append("\n");
		}
		out.print(stringBuff.toString());
		out.close();


	}
}
