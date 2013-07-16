package datamodel;

import java.io.BufferedReader;

/**
 * ModelValue is a piece of data we are interested in that emerges from the chemkin simulation.
 * 
 * This can either be product effluent fractions, a flame speed, an ignition delay, etc...
 * 
 * the container of the data is not specified here because it varies among the model value types.
 * 
 * The attribute type holds the nature of the model value.
 * 
 * Two required methods are defined: a setter for the value and a method that returns the sum of squares of the value(s).
 * @author nmvdewie
 *
 */
public abstract class ModelValue {
	
	public String type;
	
	public static final String PRODUCT_EFFLUENT = "PRODUCT_EFFLUENT";
	public static final String IGNITION_DELAY = "IGNITION_DELAY";
	public static final String FLAME_SPEED = "FLAME_SPEED";
	
	public abstract void setValue(BufferedReader bufferedReader);

	public abstract double getSSQValue();

	
}
