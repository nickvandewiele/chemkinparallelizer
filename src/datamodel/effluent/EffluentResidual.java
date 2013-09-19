package datamodel.effluent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import datamodel.ExperimentalValue;
import datamodel.ModelValue;
import datamodel.Residual;


public class EffluentResidual extends Residual {

	TYPE type = TYPE.PRODUCT_EFFLUENT; 

	public Map<String,Double> speciesFractions;
	
	public EffluentResidual(ExperimentalValue experimentalValue,
			ModelValue modelValue) {
		super.experimentalValue = experimentalValue;
		super.modelValue = modelValue;
		compute();
	}

	@Override
	public void compute() {
		speciesFractions = new HashMap<String, Double>();
		
		for(String speciesName : speciesFractions.keySet()){
			double value = ((EffluentModelValue)modelValue).speciesFractions.get(speciesName) - 
					((EffluentModelValue)modelValue).speciesFractions.get(speciesName);
			speciesFractions.put(speciesName, value);
		}

	}

	@Override
	public Double getSSQValue() {
		double sum = 0;
		for(String species : speciesFractions.keySet()){
			sum += Math.pow(speciesFractions.get(species),2);
		}
		return sum;
	}

	public Iterator<Double> createIterator() {
		return speciesFractions.values().iterator();
	}

	@Override
	public double getValue() {//should not be used!
		return -1;
	}

}
