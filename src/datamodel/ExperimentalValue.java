package datamodel;

/**
 * Abstract superclass for all experimental data
 * 
 * Constants show the possibilities for the subtypes.
 * 
 * One attribute, type, holds the type of experimental value.
 * @author nmvdewie
 *
 */
public abstract class ExperimentalValue {
	
	public static final String PRODUCT_EFFLUENT = "PRODUCT_EFFLUENT";
	public static final String IGNITION_DELAY = "IGNITION_DELAY";
	public static final String FLAME_SPEED = "FLAME_SPEED";

	public String type;

}