package datatypes;


public class ResidualFactory {

	
	public Residual createResidual(ExperimentalValue experimentalValue,
			ModelValue modelValue) {
		if(experimentalValue.type.equals(ExperimentalValue.TYPE.PRODUCT_EFFLUENT)){
			return new EffluentResidual(experimentalValue, modelValue);
		}
		else if(experimentalValue.type.equals(ExperimentalValue.TYPE.IGNITION_DELAY)){
			return new IgnitionResidual(experimentalValue, modelValue);
		}
		else if(experimentalValue.type.equals(ExperimentalValue.TYPE.FLAME_SPEED)){
			return new FlameSpeedResidual(experimentalValue, modelValue);
		}
		return null;
	}

}
