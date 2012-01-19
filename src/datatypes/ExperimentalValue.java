package datatypes;

/**
 * superclass for Ignition Delay and Flame Speed experiment
 * getters and setters implemented for attribute 'value'
 * @author nmvdewie
 *
 */
public abstract class ExperimentalValue {
	
	public static final String PRODUCT_EFFLUENT = "PRODUCT_EFFLUENT";
	public static final String IGNITION_DELAY = "IGNITION_DELAY";
	public static final String FLAME_SPEED = "FLAME_SPEED";

	public String type;

}