package parameter_estimation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * type that groups information on the experimental setup of the system
 * @author nmvdewie
 *
 */
public class Experiments {
	/**
	 * filenames of the regular reactor input, i.e. not the reactor inputs in which
	 * ignition delays are the response variable
	 */
	private LinkedList<String> RegularReactorInputs = new LinkedList<String>();
	
	/**
	 * database with all experimental info (for PFR experiments, for now)
	 */
	private String reactorSetupDB;
	
	private String pathPFRTemplate = System.getProperty("user.dir")+"/reactortemplates/PFR_template.inp";
	private String pathCSTRTemplate = System.getProperty("user.dir")+"/reactortemplates/CSTR_template.inp";
	
	// flag that shows whether a reactor database is used to construct reactor input files:
	private boolean flagReactorDB;
	
	//flag that shows which type of reactor database we are using:
	private Integer flagReactorSetupType;
	
	private String pathExperimentalDB;
	private String pathIgnitionDB;
	
	public String getPathIgnitionDB() {
		return pathIgnitionDB;
	}
	public void setPathIgnitionDB(String pathIgnitionDB) {
		this.pathIgnitionDB = pathIgnitionDB;
	}
	public String getPathExperimentalDB() {
		return pathExperimentalDB;
	}
	public void setPathExperimentalDB(String pathExperimentalDB) {
		this.pathExperimentalDB = pathExperimentalDB;
	}

	private Integer totalNoExperiments;
	private Integer noRegularExperiments;
	private LinkedList<String> responseVariables;
	private ExperimentalValues experimentalValues;

	public ExperimentalValues getExperimentalValues() {
		return experimentalValues;
	}
	public void setExperimentalValues(ExperimentalValues experimentalValues) {
		this.experimentalValues = experimentalValues;
	}
	public LinkedList<String> getResponseVariables() {
		return responseVariables;
	}
	public void setResponseVariables(LinkedList<String> responseVariables) {
		this.responseVariables = responseVariables;
	}

	/**
	 * 
	 * flags to indicate which reactor input files are dedicated to ignition delay experiments
	 * the mapping is between the filename and a boolean
	 * true = ignition delay experiment
	 * false = other type of experiment
	 */
	private Map<String,Boolean> flagIgnitionDelays = new HashMap<String,Boolean>();
	
	/**
	 * @category setter
	 * @return
	 */
	public Integer getNoRegularExperiments() {
		return totalNoExperiments - noIgnitionDelayExperiments;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setNoRegularExperiments(Integer noRegularExperiments) {
		this.noRegularExperiments = noRegularExperiments;
	}
	/**
	 * @category getter
	 * @return
	 */
	public Integer getTotalNoExperiments() {
		return totalNoExperiments;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setTotalNoExperiments(Integer totalNoExperiments) {
		this.totalNoExperiments = totalNoExperiments;
	}
	/**
	 * @category getter
	 * @return
	 */
	public Integer getNoIgnitionDelayExperiments() {
		return noIgnitionDelayExperiments;
	}
	
	/**
	 * @category setter
	 * @return
	 */
	public void setNoIgnitionDelayExperiments(Integer noIgnitionDelayExperiments) {
		this.noIgnitionDelayExperiments = noIgnitionDelayExperiments;
	}

	private Integer noIgnitionDelayExperiments;
	/**
	 * @category getter
	 * @return
	 */
	public LinkedList<String> getRegularReactorInputs() {
		return RegularReactorInputs;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setRegularReactorInputs(LinkedList<String> regularReactorInputs) {
		RegularReactorInputs = regularReactorInputs;
	}
	
	/**
	 * filenames of the reactor inputs of experiments that measure ignition delays
	 */
	private LinkedList<String> IgnitionDelayInputs = new LinkedList<String>();
	/**
	 * @category getter
	 * @return
	 */
	public LinkedList<String> getIgnitionDelayInputs() {
		return IgnitionDelayInputs;
	}
	
	/**
	 * merges RegularReactorInputs with IgnitionDelayInputs
	 * if both regular reactor inputs and ignition delay inputs are present,
	 * first the regular reactor inputs will be parsed into ReactorInputs
	 * @return
	 */
	public void mergeReactorInputs(){
		if(noRegularExperiments==0){
			ReactorInputs.addAll(IgnitionDelayInputs);
		}
		else{
			ReactorInputs.addAll(RegularReactorInputs);
			ReactorInputs.addAll(IgnitionDelayInputs);
		}
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setIgnitionDelayInputs(LinkedList<String> ignitionDelayInputs) {
		IgnitionDelayInputs = ignitionDelayInputs;
	}
	private LinkedList<String> ReactorInputs = new LinkedList<String>();

	//TODO these attributes could be modeled better, using Effluent, IgnitionDelay somehow
		/**
	 * @category getter
	 * @return
	 */
	public LinkedList<String> getReactorInputs() {
		return ReactorInputs;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setReactorInputs(LinkedList<String> reactorInputs) {
		ReactorInputs = reactorInputs;
	}
	public Experiments(){
		
	}
	public Experiments(Integer totalNoExperiments, Integer noIgnitionDelayExperiments, boolean flagReactorDB, int flagReactorSetupType){
		this.flagReactorDB = flagReactorDB;
		this.totalNoExperiments = totalNoExperiments;
		this.noIgnitionDelayExperiments = noIgnitionDelayExperiments;
		this.flagReactorSetupType = flagReactorSetupType;
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
	public Map<String, Boolean> getFlagIgnitionDelays() {
		return flagIgnitionDelays;
	}
	public void setFlagIgnitionDelays() {
		//flag true for ignition delays:
		for (int i = 0; i < noIgnitionDelayExperiments; i++){			
			//mark that these are ignition delay experiments:
			flagIgnitionDelays.put(IgnitionDelayInputs.get(i),true);
		}
		//flag false for regular experiments:
		for (int i = 0; i < noRegularExperiments; i++){			
			//mark that these are ignition delay experiments:
			flagIgnitionDelays.put(RegularReactorInputs.get(i),false);
		}
	}
	public LinkedList<String> readResponseVariables(String workingDir){
		BufferedReader in;
		responseVariables = new LinkedList<String>();
		try {
			in = new BufferedReader(new FileReader(workingDir+pathExperimentalDB));
			String dummy;
			try {
				dummy = in.readLine();
				String[] speciesArray = dummy.split(",");
				for(int i = 0; i < speciesArray.length; i++){
					responseVariables.add(speciesArray[i]);
				}
				in.close();
			} catch (IOException e) {
			}
			
		} catch (FileNotFoundException e) {
		}
		return responseVariables;						
	}
	public void printResponseVariables() throws IOException{
		PrintWriter out_species = new PrintWriter(new FileWriter("response_vars.txt"));
		for(Iterator<String> it = responseVariables.iterator(); it.hasNext();){
			out_species.println((String)it.next());
		}
		out_species.close();
	}
}
