package parameter_estimation;

import java.util.ArrayList;
import java.util.List;

import readers.ExperimentalDatabaseInput;
import readers.ExperimentalDatabaseReader;
import datamodel.ExperimentalValue;

/**
 * type that groups information on the experimental setup of the system
 * @author nmvdewie
 *
 */
public class Experiments extends Loggable{

	private ReactorInputCollector reactorInputCollector;
	private String pathPFRTemplate = System.getProperty("user.dir")+"/reactortemplates/PFR_template.inp";
	private String pathCSTRTemplate = System.getProperty("user.dir")+"/reactortemplates/CSTR_template.inp";

	public ExperimentalValue[] experimentalValues;
	public Integer total_no_experiments;
	public ExperimentalDatabaseInput[] exp_db;


	public Experiments(){
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
	
	public ExperimentalValue[] getExperimentalData() {
			readExperimentalValues();
			return experimentalValues;
			
	}

	private void readExperimentalValues() {
		List<ExperimentalValue> list = new ArrayList<ExperimentalValue>();
		experimentalValues = new ExperimentalValue[total_no_experiments];
		for (ExperimentalDatabaseInput input : exp_db){
			List<ExperimentalValue> dummy = ExperimentalDatabaseReader.read(input);
			list.addAll(dummy);
		}
		
		experimentalValues = list.toArray(new ExperimentalValue[total_no_experiments]);
	}

}
