package datamodel;

import java.util.ArrayList;
import java.util.List;


import readers.ExperimentalDatabaseInput;
import readers.ExperimentalDatabaseReader;
import util.Loggable;

/**
 * type that groups information on the experimental setup of the system
 * @author nmvdewie
 *
 */
public class Experiments extends Loggable{
	private ReactorInputCollector reactorInputCollector;
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
