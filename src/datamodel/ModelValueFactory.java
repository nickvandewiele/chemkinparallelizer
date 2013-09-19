package datamodel;

import org.apache.log4j.Logger;

import datamodel.effluent.EffluentModelValue;
import datamodel.flamespeed.FlameSpeedModelValue;
import datamodel.ignitiondelay.IgnitionDelayModelValue;
import readers.ReactorInput;

public class ModelValueFactory {
	public static Logger logger = Logger.getLogger(ModelValueFactory.class);
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
		else logger.error("Did not recognize reactor input type: "+model);
		return null;
	}
}
