package datatypes;


public abstract class Residual {
	
	public enum TYPE {PRODUCT_EFFLUENT, IGNITION_DELAY, FLAME_SPEED}
	
	public TYPE type;
	
	ExperimentalValue experimentalValue;
	
	ModelValue modelValue;
	
	public abstract void compute();

	public abstract Double getSSQValue() ;

	public abstract double getValue();
}
