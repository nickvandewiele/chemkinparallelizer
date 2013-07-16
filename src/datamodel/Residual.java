package datamodel;


public abstract class Residual {
	
	public enum TYPE {PRODUCT_EFFLUENT, IGNITION_DELAY, FLAME_SPEED}
	
	public TYPE type;
	
	protected ExperimentalValue experimentalValue;
	
	protected ModelValue modelValue;
	
	public abstract void compute();

	public abstract Double getSSQValue() ;

	public abstract double getValue();
}
