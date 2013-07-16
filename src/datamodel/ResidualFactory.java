package datamodel;

import datamodel.effluent.EffluentResidual;
import datamodel.flamespeed.FlameSpeedResidual;
import datamodel.ignitiondelay.IgnitionResidual;


public class ResidualFactory {

	
	public Residual createResidual(ExperimentalValue experimentalValue,
			ModelValue modelValue) {
		if(experimentalValue.type.equals(ExperimentalValue.PRODUCT_EFFLUENT)){
			return new EffluentResidual(experimentalValue, modelValue);
		}
		else if(experimentalValue.type.equals(ExperimentalValue.IGNITION_DELAY)){
			return new IgnitionResidual(experimentalValue, modelValue);
		}
		else if(experimentalValue.type.equals(ExperimentalValue.FLAME_SPEED)){
			return new FlameSpeedResidual(experimentalValue, modelValue);
		}
		return null;
	}

}
