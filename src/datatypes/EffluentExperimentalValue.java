package datatypes;

import java.util.Map;

import datatypes.ExperimentalValue.TYPE;


/**
 * superclass for Ignition Delay and Flame Speed experiment
 * getters and setters implemented for attribute 'value'
 * @author nmvdewie
 *
 */
public class EffluentExperimentalValue extends ExperimentalValue {
	TYPE type = ExperimentalValue.TYPE.PRODUCT_EFFLUENT;

	public Map<String,Double> speciesFractions;
	
	public EffluentExperimentalValue(Map<String,Double> speciesFractions){
		this.speciesFractions = speciesFractions;
	}

}