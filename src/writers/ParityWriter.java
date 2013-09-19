package writers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import util.Paths;

import datamodel.ExperimentalValue;
import datamodel.ModelValue;
import datamodel.effluent.EffluentModelValue;
import datamodel.effluent.EffluentExperimentalValue;
import datamodel.flamespeed.FlameSpeedExperimentalValue;
import datamodel.flamespeed.FlameSpeedModelValue;
import datamodel.ignitiondelay.IgnitionDelayExperimentalValue;
import datamodel.ignitiondelay.IgnitionDelayModelValue;

public class ParityWriter {

	Paths paths;

	ExperimentalValue[] experimentalValues;
	ModelValue[] modelValues;
	List<String> speciesNames;
	public ParityWriter(ExperimentalValue[] experimentalValues, ModelValue[] modelValues,
			List<String> speciesNames) {
		this.paths = paths;
		this.experimentalValues = experimentalValues;
		this.modelValues = modelValues;
		this.speciesNames = speciesNames;
	}

	public void write() throws IOException {
		writeSpeciesFractions();
		writeIgnitionDelays();
		writeFlameSpeeds();;

	}

	private void writeSpeciesFractions() {
		String s = " , ,"+"Experimental Value ,"+"Model Value ,"+"Experimental Value\n";
		for(int j=0;j<speciesNames.size();j++){//order per species: all experiments
			for(int i = 0; i < experimentalValues.length; i++){
				ExperimentalValue experimentalValue = experimentalValues[i];
				ModelValue modelValue = modelValues[i];

				if(experimentalValue.type.equals(ExperimentalValue.PRODUCT_EFFLUENT)){
					Double experiment_value = ((EffluentExperimentalValue)experimentalValue).speciesFractions.get(speciesNames.get(j));
					Double model_value = ((EffluentModelValue)modelValue).getSpeciesFractions().get(speciesNames.get(j));
					//out.println(speciesNames.get(i));
					if(model_value != null && experiment_value != null){
						s+=speciesNames.get(j).toString()+",";//comma delimited
						s+="experiment no. "+(i+1)+","+experiment_value+","+model_value+","+experiment_value+"\n";
					}
				}
			}
		}
		File file = new File(Paths.getWorkingDir()+"SpeciesParity.csv");
		if(!s.equals(" , ,"+"Experimental Value ,"+"Model Value ,"+"Experimental Value\n")) write(file,s);

	}

	private void writeFlameSpeeds() {
		String s = "";

		for(int i = 0; i < experimentalValues.length; i++){
			ExperimentalValue experimentalValue = experimentalValues[i];
			ModelValue modelValue = modelValues[i];

			if(experimentalValue.type.equals(ExperimentalValue.FLAME_SPEED)){
				s+="Flame Speed:\t"+((FlameSpeedExperimentalValue)experimentalValue).value+"\t"+((FlameSpeedModelValue)modelValue).value+","+((FlameSpeedExperimentalValue)experimentalValue).value+"\n";
			}
		}
		File file = new File(Paths.getWorkingDir()+"FlameSpeedParity.csv");
		if(!s.isEmpty()) write(file,s);

	}

	private void writeIgnitionDelays() {
		String s = "";

		for(int i = 0; i < experimentalValues.length; i++){
			ExperimentalValue experimentalValue = experimentalValues[i];
			ModelValue modelValue = modelValues[i];

			if(experimentalValue.type.equals(ExperimentalValue.IGNITION_DELAY)){
				s+="Ignition Delay:\t"+((IgnitionDelayExperimentalValue)experimentalValue).value+"\t"+((IgnitionDelayModelValue)modelValue).value+","+((IgnitionDelayExperimentalValue)experimentalValue).value+"\n";
			}
		}
		File file = new File(Paths.getWorkingDir()+"IgnitionDelayParity.csv");
		if(!s.isEmpty()) write(file,s);

	}

	private void write(File file, String s) {

		try {
			FileUtils.writeStringToFile(file, s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
