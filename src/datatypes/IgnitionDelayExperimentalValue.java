package datatypes;


/**
 * type for ignition delay, as in shock tube experiments. Is considered as response variable which can be used in
 * optimizations 
 * @author nmvdewie
 *
 */
public class IgnitionDelayExperimentalValue extends ExperimentalValue {
	public IgnitionDelayExperimentalValue(double value) {
		this.value = value;
	}

	TYPE type = ExperimentalValue.TYPE.IGNITION_DELAY;

	public double value;
}
