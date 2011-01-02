package parameter_estimation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * type that groups information on the experimental setup of the system
 * @author nmvdewie
 *
 */
public class Experiments extends Loggable{

	/**
	 * database with all experimental info (for PFR experiments, for now)
	 */
	private String reactorSetupDB;
	private ReactorInputCollector reactorInputCollector;
	private String pathPFRTemplate = System.getProperty("user.dir")+"/reactortemplates/PFR_template.inp";
	private String pathCSTRTemplate = System.getProperty("user.dir")+"/reactortemplates/CSTR_template.inp";

	// flag that shows whether a reactor database is used to construct reactor input files:
	private boolean flagReactorDB;

	//flag that shows which type of reactor database we are using:
	private Integer flagReactorSetupType;

	private String pathExperimentalDB;
	private String pathIgnitionDB;
	private String pathFlameSpeedDB;

	private ResponseVariables responseVariables;
	private ExperimentalValues experimentalValues;


	public Experiments(){
		this.responseVariables = new ResponseVariables();
	}

	public Experiments(boolean flagReactorDB, int flagReactorSetupType){
		super();
		this.flagReactorDB = flagReactorDB;
		this.flagReactorSetupType = flagReactorSetupType;
		
	}



	public ExperimentalValues readExperimentalData(String workingDir){
		experimentalValues = new ExperimentalValues();
		
		//get experimental effluent values, if they exist:
		if(!(getPathExperimentalDB().equals(""))){
			try {		
				//read experimental database file:
				String path = workingDir+getPathExperimentalDB();
				BufferedReader in =  new BufferedReader(new FileReader(path));
				
				//set effluent responses:
				LinkedList<String> effluentResponses = responseVariables.readEffluentResponses(in);
				responseVariables.setEffluentResponses(effluentResponses);
				
				//reopen experimental database file:
				in =  new BufferedReader(new FileReader(path));
				LinkedList<Map<String,Double>> list = readExperimentalEffluents(in,
						getReactorInputCollector().getNoRegularExperiments());
				experimentalValues.setExperimentalEffluentValues(list);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}

		if(!(getPathIgnitionDB().equals(""))){
			try {
				//set ignition Delay in response variables to TRUE:
				responseVariables.setIgnitionDelay(true);
				//read igntion delay database file:
				String path = workingDir+getPathIgnitionDB();
				BufferedReader in =  new BufferedReader(new FileReader(path));
				LinkedList<Double> list = readExperimentalIgnitionDelays(in,
						getReactorInputCollector().getNoIgnitionDelayExperiments());
				in.close();
				experimentalValues.setExperimentalIgnitionValues(list);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}

		if(!(getPathFlameSpeedDB().equals(""))){
			try {
				//set  flame speed in response variables to TRUE:
				responseVariables.setFlameSpeed(true);
				
				//read flame speed database file:
				String path = workingDir+getPathFlameSpeedDB();
				BufferedReader in =  new BufferedReader(new FileReader(path));
				LinkedList<Double> list = readExperimentalFlameSpeeds(in,
						getReactorInputCollector().getNoFlameSpeedExperiments());
				in.close();
				experimentalValues.setExperimentalFlameSpeedValues(list);
			} catch (IOException e) {
				e.printStackTrace();
			}	
		}

		return experimentalValues;
	}
	private LinkedList<Double> readExperimentalFlameSpeeds(BufferedReader in,
			Integer noFlameSpeedExperiments) {
		LinkedList<Double> flameSpeeds = new LinkedList<Double>();
		try {
			//read in species names on first line:
			String dummy = in.readLine();
			while(dummy!=null){
				flameSpeeds.add(new Double(dummy));
				//expMassFractions.clear();
				dummy = in.readLine();
			}
			if (flameSpeeds.size()!= noFlameSpeedExperiments){
				logger.debug("Experimental Flame Speed Database a different number of experiments than specified in INPUT file! Maybe check if .csv is created with redundand commas at the end...");
				System.exit(-1);
			}
			in.close();
		} catch(IOException e){
			logger.error("Something went wrong during the preprocessing of the Flame Speed experimental data file!",e);
			System.exit(-1);
		}
		if((flameSpeeds.size()==noFlameSpeedExperiments)){
			return flameSpeeds;
		}
		else{
			logger.debug("Flame Speed Experiments database contains different no. of experiments as defined in INPUT.txt!");
			System.exit(-1);
			return null;	
		}
	}

	/**
	 * list of ignition delays is a single column
	 * @param in TODO
	 * @param noIgnitionDelayExperiments
	 * @return
	 * @throws IOException
	 */
	private LinkedList<Double> readExperimentalIgnitionDelays (BufferedReader in, int noIgnitionDelayExperiments)throws IOException{
		LinkedList<Double> ignitionDelays = new LinkedList<Double>();
		try {
			//read in species names on first line:
			String dummy = in.readLine();
			while(dummy!=null){
				ignitionDelays.add(new Double(dummy));
				//expMassFractions.clear();
				dummy = in.readLine();
			}
			if (ignitionDelays.size()!= noIgnitionDelayExperiments){
				logger.debug("Experimental Ignition Delay Database a different number of Ignition Delay  experiments than specified in INPUT file! Maybe check if .csv is created with redundand commas at the end...");
				System.exit(-1);
			}
			in.close();
		} catch(IOException e){
			logger.error("Something went wrong during the preprocessing of the Ignition Delay  experimental data file!",e);
			System.exit(-1);
		}
		if((ignitionDelays.size()==noIgnitionDelayExperiments)){
			return ignitionDelays;
		}
		else{
			logger.debug("Experiments database contains different no. of experiments as defined in INPUT.txt!");
			System.exit(-1);
			return null;	
		}

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
	 * @param in TODO
	 * @return List of the experiments, with molar flowrates of the response variables
	 * @throws IOException
	 */
	public LinkedList<Map<String, Double>> readExperimentalEffluents (BufferedReader in, int noRegularExperiments)throws IOException{
		LinkedList<Map<String, Double>> exp = new LinkedList<Map<String, Double>>();
		try {

			//read in species names on first line:
			String species_names = in.readLine();
			//System.out.println(species_names);
			String[] st_species = species_names.split(",");
			String dummy = in.readLine();
			//System.out.println(dummy);

			Map <String, Double> expMassFractions;
			while(dummy!=null){
				String[] st_dummy = dummy.split(",");
				expMassFractions = new HashMap <String, Double>();
				for (int j = 0; j < st_species.length; j++) {
					expMassFractions.put(st_species[j],Double.parseDouble(st_dummy[j]));	
				}
				exp.add(expMassFractions);
				//expMassFractions.clear();
				dummy = in.readLine();

			}
			if (exp.size()!= noRegularExperiments){
				logger.debug("Experimental Database a different number of experiments than specified in INPUT file! Maybe check if .csv is created with redundand commas at the end...");
				System.exit(-1);
			}
			in.close();
		} catch(IOException e){
			logger.error("Something went wrong during the preprocessing of the experimental data file!",e);
			System.exit(-1);
		}
		if((exp.size() == noRegularExperiments)){
			return exp;
		}
		else{
			logger.debug("Experiments database contains different no. of experiments as defined in main class!");
			System.exit(-1);
			return null;	
		}
	}



	/**
	 * ####################
	 * GETTERS AND SETTERS:
	 * ####################
	 */

	/**
	 * @category getter
	 * @return
	 */
	public ReactorInputCollector getReactorInputCollector() {
		return reactorInputCollector;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setReactorInputCollector(ReactorInputCollector reactorInputCollector) {
		this.reactorInputCollector = reactorInputCollector;
	}
	/**
	 * @category getter
	 * @return
	 */
	public String getReactorSetupDB() {
		return reactorSetupDB;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setReactorSetupDB(String reactorSetupDB) {
		this.reactorSetupDB = reactorSetupDB;
	}
	/**
	 * @category getter
	 * @return
	 */
	public boolean isFlagReactorDB() {
		return flagReactorDB;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setFlagReactorDB(boolean flagReactorDB) {
		this.flagReactorDB = flagReactorDB;
	}
	/**
	 * @category getter
	 * @return
	 */
	public String getPathPFRTemplate() {
		return pathPFRTemplate;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setPathPFRTemplate(String pathPFRTemplate) {
		this.pathPFRTemplate = pathPFRTemplate;
	}
	/**
	 * @category getter
	 * @return
	 */
	public String getPathCSTRTemplate() {
		return pathCSTRTemplate;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setPathCSTRTemplate(String pathCSTRTemplate) {
		this.pathCSTRTemplate = pathCSTRTemplate;
	}
	/**
	 * @category getter
	 * @return
	 */
	public Integer getFlagReactorSetupType() {
		return flagReactorSetupType;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setFlagReactorSetupType(Integer flagReactorSetupType) {
		this.flagReactorSetupType = flagReactorSetupType;
	}
	/**
	 * @category getter
	 * @return
	 */
	public ExperimentalValues getExperimentalValues() {
		return experimentalValues;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setExperimentalValues(ExperimentalValues experimentalValues) {
		this.experimentalValues = experimentalValues;
	}
	/**
	 * @category getter
	 * @return
	 */
	public String getPathIgnitionDB() {
		return pathIgnitionDB;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setPathIgnitionDB(String pathIgnitionDB) {
		this.pathIgnitionDB = pathIgnitionDB;
	}
	/**
	 * @category getter
	 * @return
	 */
	public String getPathExperimentalDB() {
		return pathExperimentalDB;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setPathExperimentalDB(String pathExperimentalDB) {
		this.pathExperimentalDB = pathExperimentalDB;
	}

	/**
	 * @category setter
	 * @return
	 */
	public void setPathFlameSpeedDB(String readLine) {
		this.pathFlameSpeedDB = readLine;

	}

	/**
	 * @category getter
	 * @return
	 */
	public String getPathFlameSpeedDB() {
		return pathFlameSpeedDB;
	}
	
	/**
	 * @category getter
	 * @return
	 */
	public ResponseVariables getResponseVariables() {
		return responseVariables;
	}
	
	/**
	 * @category setter
	 * @param responseVariables
	 */
	public void setResponseVariables(ResponseVariables responseVariables) {
		this.responseVariables = responseVariables;
	}

}
