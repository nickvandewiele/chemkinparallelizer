package parameter_estimation;

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
		if(model.equals(ReactorSetupInput.MODEL.PFR)){
			return new EffluentModelValue();
		}
		else if(model.equals(ReactorSetupInput.MODEL.CSTR)){
			return new EffluentModelValue();
		}
		else if(model.equals(ReactorSetupInput.MODEL.IGNITION_DELAY)){
			return new IgnitionDelayModelValue();
		}
		else if(model.equals(ReactorSetupInput.MODEL.FLAME_SPEED)){
			return new FlameSpeedModelValue();
		}
		
		return null;
	}
}
