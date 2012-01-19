package datatypes;

import readers.ReactorInput;
import readers.ReactorSetupInput;

public class ModelValueFactory {
	
	String model;
	
	public ModelValueFactory(String model2){
		this.model = model2;
	}
	public ModelValue createEffluentModelValue() {
		return null;
	}
	
	public ModelValue createIgnitionDelayModelValue() {
		return null;
	}
	
	public ModelValue createFlameSpeedtModelValue() {
		return null;
	}
	
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
