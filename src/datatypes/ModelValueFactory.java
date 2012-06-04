package datatypes;

import readers.ReactorInput;
import readers.ReactorSetupInput;

public class ModelValueFactory {
	
	String model;
	
	public ModelValueFactory(String model2){
		this.model = model2;
	}
	
	/**
	 * A model value is created based on the type of reactor setup that is specified. 
	 * 
	 * For example, in a PFR reactor product effluent data can be collected, but flame speeds, or 
	 * ignition delays cannot be measured in this type of reactor.
	 * 
	 * 
	 * @return
	 */
	public ModelValue createModelValue(){
		if(model.equals(ReactorInput.PFR)){
			return new EffluentModelValue();
		}
		else if(model.equals(ReactorInput.CSTR)){
			return new EffluentModelValue();
		}
		else if(model.equals(ReactorInput.IGNITION_DELAY)){
			return new IgnitionDelayModelValue();
		}
		else if(model.equals(ReactorInput.FLAME_SPEED)){
			return new FlameSpeedModelValue();
		}
		
		return null;
	}
}
