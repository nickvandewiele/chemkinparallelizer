package datamodel.effluent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import util.Tools;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import datamodel.ModelValue;

public class EffluentModelValue extends ModelValue {

	public Map<String,Double> speciesFractions;

	public EffluentModelValue() {
		super();
		type = ModelValue.PRODUCT_EFFLUENT;
		speciesFractions = new HashMap<String, Double>();
	}

	/**
	 * @category getter
	 * @return
	 */
	public Map<String, Double> getSpeciesFractions() {
		return speciesFractions;
	}

	/**
	 * @category setter
	 * @return
	 */
	public void setSpeciesFractions(Map<String, Double> speciesFractions) {
		this.speciesFractions = speciesFractions;
	}

	@Override
	public double getSSQValue() {
		double sum = 0;
		for(String species : speciesFractions.keySet()){
			sum += Math.pow(speciesFractions.get(species),2);
		}
		return sum;
	}

	@Override
	public void setValue(File f) {
		read(f);
	
	}

	/**
	 * read_ckcsv should read the CKSoln.ckcsv file and retrieve data from it.<BR>
	 * Which data specifically is explained here below:<BR>
	 * 	<LI>the mole fraction of all species</LI>
	 * the values should be taken at the end point of the reactor, i.e. the last data value of each row in the .ckcsv file<BR>
	 * the data will be stored in a LinkedList, chosen for its flexibility<BR>
	 * @param f TODO
	 * @throws IOException
	 */
	public void read(File f) {		
	
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(f, "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : lines) {
			Iterable<String> results = Splitter.on(CharMatcher.anyOf(","))
					.trimResults().omitEmptyStrings().split(line);
			String[] pieces = Iterables.toArray(results, String.class);
			if (line.startsWith("Mass_fraction_")) {
				speciesFractions.put(pieces[0], Double.parseDouble(pieces[pieces.length-1]));
			}
			//convert to a HashMap with the real species names (cut of Mass_fraction_ or Mole_fraction_:
			speciesFractions = Tools.cutOffMassFrac_(speciesFractions);
		}
		
	}

}