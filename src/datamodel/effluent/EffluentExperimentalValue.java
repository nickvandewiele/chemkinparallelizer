package datamodel.effluent;

import java.util.Map;

import datamodel.ExperimentalValue;



/**
 * superclass for Ignition Delay and Flame Speed experiment
 * getters and setters implemented for attribute 'value'
 * @author nmvdewie
 *
 */
public class EffluentExperimentalValue extends ExperimentalValue {
	
	public Map<String,Double> speciesFractions;
	
	public EffluentExperimentalValue(Map<String,Double> speciesFractions){
		this.speciesFractions = speciesFractions;
		super.type = ExperimentalValue.PRODUCT_EFFLUENT;
	}

}