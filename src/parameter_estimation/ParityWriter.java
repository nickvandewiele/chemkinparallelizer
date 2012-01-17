package parameter_estimation;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import datatypes.EffluentExperimentalValue;
import datatypes.EffluentModelValue;
import datatypes.ExperimentalValue;
import datatypes.FlameSpeedExperimentalValue;
import datatypes.FlameSpeedModelValue;
import datatypes.IgnitionDelayExperimentalValue;
import datatypes.IgnitionDelayModelValue;
import datatypes.ModelValue;

public class ParityWriter {

	Paths paths;

	ExperimentalValue[] experimentalValues;
	ModelValue[] modelValues;
	List<String> speciesNames;
	public ParityWriter(Paths paths, ExperimentalValue[] experimentalValues,
			ModelValue[] modelValues, List<String> speciesNames) {
		this.paths = paths;
		this.experimentalValues = experimentalValues;
		this.modelValues = modelValues;
		this.speciesNames = speciesNames;
	}

	public void write() {
		try {
			PrintWriter out_effluent = new PrintWriter(new FileWriter(paths.getWorkingDir()+"SpeciesParity.csv"));
			PrintWriter out_ignition = new PrintWriter(new FileWriter(paths.getWorkingDir()+"IgnitionDelayParity.csv"));
			PrintWriter out_flame_speed = new PrintWriter(new FileWriter(paths.getWorkingDir()+"FlameSpeedParity.csv"));

			for(int i = 0; i < experimentalValues.length; i++){

				ExperimentalValue experimentalValue = experimentalValues[i];
				ModelValue modelValue = modelValues[i];

				if(experimentalValue.type.equals(ExperimentalValue.TYPE.PRODUCT_EFFLUENT)){
					StringBuffer stringBuff = new StringBuffer();
					stringBuff.append("Experiment: "+"\t"+"Experimental Value"+"\t"+"Model Value"+"\t"+"Experimental Value\n");
					// loop through all species:
					for(int j=0;j<speciesNames.size();j++){
						out_effluent.println(speciesNames.get(j).toString());
						// loop through all experiments:
						Double experiment_value = ((EffluentExperimentalValue)experimentalValue).speciesFractions.get(speciesNames.get(j));
						Double model_value = ((EffluentModelValue)modelValue).getSpeciesFractions().get(speciesNames.get(j));
						//out.println(speciesNames.get(i));
						stringBuff.append("experiment no. "+i+","+experiment_value+","+model_value+","+experiment_value+"\n");
					}
					stringBuff.append("\n");
					out_effluent.println(stringBuff.toString());
				}
				else if(experimentalValue.type.equals(ExperimentalValue.TYPE.IGNITION_DELAY)){
					out_ignition.println("Ignition Delay:\t"+((IgnitionDelayExperimentalValue)experimentalValue).value+"\t"+((IgnitionDelayModelValue)modelValue).value+","+((IgnitionDelayExperimentalValue)experimentalValue).value);
				}
				else if(experimentalValue.type.equals(ExperimentalValue.TYPE.FLAME_SPEED)){
					out_flame_speed.println("Flame Speed:\t"+((FlameSpeedExperimentalValue)experimentalValue).value+"\t"+((FlameSpeedModelValue)modelValue).value+","+((FlameSpeedExperimentalValue)experimentalValue).value);
				}
			}

			out_effluent.close();
			out_ignition.close();
			out_flame_speed.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};

	}

}
