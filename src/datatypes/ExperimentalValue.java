package datatypes;

/**
 * superclass for Ignition Delay and Flame Speed experiment
 * getters and setters implemented for attribute 'value'
 * @author nmvdewie
 *
 */
public abstract class ExperimentalValue {
	public enum TYPE {PRODUCT_EFFLUENT, IGNITION_DELAY, FLAME_SPEED}

	public TYPE type;

}