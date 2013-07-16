package datamodel.effluent;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import datamodel.ModelValue;

import parameter_estimation.Tools;
/**
 * type that unites effluent information of a reactor type, i.e.
 * effluent composition, species names
 * @author nmvdewie
 *
 */
public class EffluentModelValue extends ModelValue{
 
	public Map<String,Double> speciesFractions;
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
	public EffluentModelValue(){
		type = ModelValue.PRODUCT_EFFLUENT;
		speciesFractions = new HashMap<String, Double>();
	}
	@Override
	public void setValue(BufferedReader bufferedReader) {
		readCkcsvEffluent(bufferedReader);

	}
	/**
	 * read_ckcsv should read the CKSoln.ckcsv file and retrieve data from it.<BR>
	 * Which data specifically is explained here below:<BR>
	 * 	<LI>the mole fraction of all species</LI>
	 * the values should be taken at the end point of the reactor, i.e. the last data value of each row in the .ckcsv file<BR>
	 * the data will be stored in a LinkedList, chosen for its flexibility<BR>
	 * @param in TODO
	 * @throws IOException
	 */
	public void readCkcsvEffluent (BufferedReader in) {		

		String temp;
		String [] st_temp;
		LinkedList<String> list_temp;

		/*
		 *  Looking for the String "Exit_mass_flow_rate" since the line right after this line,
		 *  will contain the first species' mass fractions
		 */
		try {
			list_temp = new LinkedList<String>();
			do {
				list_temp.clear();

				temp = in.readLine();

				st_temp = temp.split(", ");
				for (int i=0;i<st_temp.length;i++){
					list_temp.add(st_temp[i]);
				}
			} while (!(list_temp.get(0)).equals("Exit_mass_flow_rate"));


			/* read all species' mass fractions, number of species is unknown, until the String "Molecular_weight" is encountered,
			 * which implies that the end of the list with species' mass fractions has been reached.
			 */
			list_temp.clear();
			list_temp = new LinkedList<String>();
			do {
				list_temp.clear();
				temp =in.readLine();
				st_temp = temp.split(", ");
				for (int i=0;i<st_temp.length;i++){
					list_temp.add(st_temp[i]);
				}
				if(!(list_temp.get(0)).equals("Molecular_weight")){				
					speciesFractions.put((String)list_temp.get(0), Double.parseDouble(list_temp.get(list_temp.size()-1)));

				}
			} while (!(list_temp.get(0)).equals("Molecular_weight"));


			in.close();
			//convert to a HashMap with the real species names (cut of Mass_fraction_ or Mole_fraction_:
			speciesFractions = Tools.cutOffMassFrac_(speciesFractions);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public double getSSQValue() {
		double sum = 0;
		for(String species : speciesFractions.keySet()){
			sum += Math.pow(speciesFractions.get(species),2);
		}
		return sum;
	}
}

