package parameter_estimation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * ModelValues combines effluent data and ignition data
 * @author nmvdewie
 *
 */
public class ModelValues extends Loggable{
	public ModelValues(){
		
	}
	public ModelValues(LinkedList<Map<String,Double>> modelEffluentValues, LinkedList<Double> modelIgnitionValues){
		this.modelEffluentValues = modelEffluentValues;
		this.modelIgnitionValues = modelIgnitionValues;
	}
	private LinkedList<Map<String,Double>> modelEffluentValues = new LinkedList<Map<String,Double>>();
	public LinkedList<Map<String, Double>> getModelEffluentValues() {
		return modelEffluentValues;
	}
	public void setModelEffluentValues(
			LinkedList<Map<String, Double>> modelEffluentValues) {
		this.modelEffluentValues = modelEffluentValues;
	}
	public LinkedList<Double> getModelIgnitionValues() {
		return modelIgnitionValues;
	}
	public void setModelIgnitionValues(LinkedList<Double> modelIgnitionValues) {
		this.modelIgnitionValues = modelIgnitionValues;
	}
	private LinkedList<Double> modelIgnitionValues = new LinkedList<Double>();
	private LinkedList<Double> modelFlameSpeedValues = new LinkedList<Double>();
	public LinkedList<Double> getModelFlameSpeedValues() {
		return modelFlameSpeedValues;
	}
	public void setModelFlameSpeedValues(LinkedList<Double> modelFlameSpeedValues) {
		this.modelFlameSpeedValues = modelFlameSpeedValues;
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
	public static Map<String, Double> readCkcsvEffluent (BufferedReader in) throws IOException {
		Map <String, Double> dummy = new HashMap<String, Double>();
		
	
		String temp;
		String [] st_temp;
		LinkedList<String> list_temp;
	
		/*
		 *  Looking for the String "Exit_mass_flow_rate" since the line right after this line,
		 *  will contain the first species' mass fractions
		 */
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
				dummy.put((String)list_temp.get(0), Double.parseDouble(list_temp.get(list_temp.size()-1)));
	
			}
		} while (!(list_temp.get(0)).equals("Molecular_weight"));
	
	
		in.close();
		//convert to a HashMap with the real species names (cut of Mass_fraction_ or Mole_fraction_:
		dummy = Tools.cutOffMassFrac_(dummy);
		return dummy;
	}
	public static Double readCkcsvFlameSpeed(BufferedReader in){
		Double flameSpeed = readCkcsv(in, "Flame_speed");
		return flameSpeed;
	}
	public static Double readCkcsv(BufferedReader in, String CkcsvType){
		
		Double value = null;
		try {
			String temp;
			String [] st_temp;
			LinkedList<String> list_temp;
			list_temp = new LinkedList<String>();
			do {
				list_temp.clear();
				try {
					temp = in.readLine();
					st_temp = temp.split(", ");
					for (int i=0;i<st_temp.length;i++){
						list_temp.add(st_temp[i]);
					}
					
				} catch (IOException e) {
					logger.debug(e);
				}
			} while (!(list_temp.get(0)).equals(CkcsvType));
			in.close();
			//take last value in row:
			value = new Double(list_temp.get(list_temp.size()-1));
			
		} catch (FileNotFoundException e) {
			logger.debug(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return value;
	}
	/**
	 * will search for String "Ignition_time_1_by_max_dT/dt" and return value of it
	 * @param in TODO
	 * @return
	 */
	public static Double readCkcsvIgnitionDelay(BufferedReader in) {
		Double ignitionDelay = ModelValues.readCkcsv(in, "Ignition_time_1_by_max_dT/dt");
		return ignitionDelay;
	}

}
