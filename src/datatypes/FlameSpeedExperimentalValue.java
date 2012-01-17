package datatypes;


public class FlameSpeedExperimentalValue extends ExperimentalValue {
	public FlameSpeedExperimentalValue(Double double1) {
		this.value = double1;
	}

	TYPE type = ExperimentalValue.TYPE.FLAME_SPEED;

	public double value;


}
