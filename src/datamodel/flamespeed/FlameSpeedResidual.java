package datamodel.flamespeed;

import datamodel.ExperimentalValue;
import datamodel.ModelValue;
import datamodel.Residual;


public class FlameSpeedResidual extends Residual {

	public double value; 
	
	public FlameSpeedResidual(ExperimentalValue experimentalValue,
			ModelValue modelValue) {
		super.experimentalValue = experimentalValue;
		super.modelValue = modelValue;
		compute();
	}

	@Override
	public void compute() {
		value = ((FlameSpeedModelValue)modelValue).value - ((FlameSpeedExperimentalValue)experimentalValue).value; 

	}

	@Override
	public Double getSSQValue() {
		return Math.pow(value,2);
	}

	@Override
	public double getValue() {
		return value;
	}

}
