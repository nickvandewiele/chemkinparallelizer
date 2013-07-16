package datamodel.ignitiondelay;

import datamodel.ExperimentalValue;


/**
 * type for ignition delay, as in shock tube experiments. Is considered as response variable which can be used in
 * optimizations 
 * @author nmvdewie
 *
 */
public class IgnitionDelayExperimentalValue extends ExperimentalValue {
	public IgnitionDelayExperimentalValue(double value) {
		super.type = ExperimentalValue.IGNITION_DELAY;

		this.value = value;
	}

	public double value;
}
