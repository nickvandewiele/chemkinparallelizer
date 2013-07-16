package datamodel.flamespeed;

import datamodel.ExperimentalValue;


public class FlameSpeedExperimentalValue extends ExperimentalValue {
	public FlameSpeedExperimentalValue(Double double1) {
		this.value = double1;
		super.type = ExperimentalValue.FLAME_SPEED;
	}

	
	public double value;


}
