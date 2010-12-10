package parameter_estimation;

/**
 * type for ignition delay, as in shock tube experiments. Is considered as response variable which can be used in
 * optimizations 
 * @author nmvdewie
 *
 */
public class IgnitionDelay {
	public Double value;

	public IgnitionDelay(){
	}
	
	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}
}
