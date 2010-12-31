package parameter_estimation;

/**
 * superclass for Ignition Delay and Flame Speed experiment
 * getters and setters implemented for attribute 'value'
 * @author nmvdewie
 *
 */
public class Experiment {

	Double value;

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public Experiment() {
		super();
	}

}