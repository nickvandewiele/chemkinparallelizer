package datamodel.ignitiondelay;

import datamodel.ExperimentalValue;
import datamodel.ModelValue;
import datamodel.Residual;


public class IgnitionResidual extends Residual {

	public double value;
	
	public IgnitionResidual(ExperimentalValue experimentalValue,
			ModelValue modelValue) {
		super.experimentalValue = (IgnitionDelayExperimentalValue)experimentalValue;
		super.modelValue = (IgnitionDelayModelValue)modelValue;
		compute();
	}

	@Override
	public void compute() {
		value = ((IgnitionDelayModelValue)modelValue).value - ((IgnitionDelayExperimentalValue)experimentalValue).value; 

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
