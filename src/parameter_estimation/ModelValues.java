package parameter_estimation;

import java.util.LinkedList;
import java.util.Map;

/**
 * ModelValues combines effluent data and ignition data
 * @author nmvdewie
 *
 */
public class ModelValues {
	public ModelValues(){
		
	}
	public ModelValues(LinkedList<Map<String,Double>> modelEffluentValues, LinkedList<Double> modelIgnitionValues){
		this.modelEffluentValues = modelEffluentValues;
		this.modelIgnitionValues = modelIgnitionValues;
	}
	private LinkedList<Map<String,Double>> modelEffluentValues = new LinkedList<Map<String,Double>>();
	public LinkedList<Map<String, Double>> getModelEffluentValues() {
		return modelEffluentValues;
	}
	public void setModelEffluentValues(
			LinkedList<Map<String, Double>> modelEffluentValues) {
		this.modelEffluentValues = modelEffluentValues;
	}
	public LinkedList<Double> getModelIgnitionValues() {
		return modelIgnitionValues;
	}
	public void setModelIgnitionValues(LinkedList<Double> modelIgnitionValues) {
		this.modelIgnitionValues = modelIgnitionValues;
	}
	private LinkedList<Double> modelIgnitionValues = new LinkedList<Double>();
	private LinkedList<Double> modelFlameSpeedValues = new LinkedList<Double>();
	public LinkedList<Double> getModelFlameSpeedValues() {
		return modelFlameSpeedValues;
	}
	public void setModelFlameSpeedValues(LinkedList<Double> modelFlameSpeedValues) {
		this.modelFlameSpeedValues = modelFlameSpeedValues;
	}

}
