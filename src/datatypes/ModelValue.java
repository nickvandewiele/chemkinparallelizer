package datatypes;

import java.io.BufferedReader;

/**
 * ModelValue is a piece of data we are interested in that emerges from the chemkin simulation.
 * 
 * This can either be product effluent fractions, a flame speed, an ignition delay, etc...
 * 
 * the container of the data is not specified here because it varies among the model value types.
 * 
 * @author nmvdewie
 *
 */
public abstract class ModelValue {
	public enum TYPE {PRODUCT_EFFLUENT, IGNITION_DELAY, FLAME_SPEED}
	
	public TYPE type;
	
	public abstract void setValue(BufferedReader bufferedReader);

	public abstract double getSSQValue();

	
}
