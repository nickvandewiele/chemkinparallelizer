package readers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import datatypes.EffluentExperimentalValue;
import datatypes.ExperimentalValue;
import datatypes.FlameSpeedExperimentalValue;
import datatypes.IgnitionDelayExperimentalValue;

public class ExperimentalDatabaseReader {
	static Logger logger = Logger.getLogger(ExperimentalDatabaseReader.class);
	
	public static List<ExperimentalValue> read(ExperimentalDatabaseInput input) {
		if(input.type.equals(ExperimentalDatabaseInput.TYPE.YIELDS)){
			//read experimental database file:
			try {
				BufferedReader in =  new BufferedReader(new FileReader(input.location));
				return readExperimentalEffluents(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if(input.type.equals(ExperimentalDatabaseInput.TYPE.IGNITION_DELAY)){
			try {
				BufferedReader in =  new BufferedReader(new FileReader(input.location));
				return readExperimentalIgnitionDelays(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if(input.type.equals(ExperimentalDatabaseInput.TYPE.FLAME_SPEED)){
			try {
				BufferedReader in =  new BufferedReader(new FileReader(input.location));
				return readExperimentalFlameSpeeds(in);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * experiments_parser preprocesses the experimental data file into a easy-to-use format List<Map><String,Double>
	 * It reads the experimental data file. The format of this file (.csv) ought to be as follows:
	 * 	-first line states all the variable names, separated by commas
	 * 	-each of the following lines contain the experimental response variables, in the same order as the variable
	 *   names were declared
	 *  Each experiment is stored in a List with HashMaps as elements (see further)
	 *  The routines cuts each line in pieces, using the comma as separator
	 *  For each experiment, the variable name and the molar flow rate of each variable are put in a HashMap	 *   
	 * @param in TODO
	 * @return List of the experiments, with molar flowrates of the response variables
	 * @throws IOException
	 */
	private static List<ExperimentalValue> readExperimentalEffluents (BufferedReader in){
		List<ExperimentalValue> exp = new ArrayList<ExperimentalValue>();
		try {

			//read in species names on first line:
			String species_names = in.readLine();
			//System.out.println(species_names);
			String[] st_species = species_names.split(",");
			String dummy = in.readLine();
			//System.out.println(dummy);

			Map <String, Double> expMassFractions;
			while(dummy!=null){
				String[] st_dummy = dummy.split(",");
				expMassFractions = new HashMap <String, Double>();
				for (int j = 0; j < st_species.length; j++) {
					expMassFractions.put(st_species[j],Double.parseDouble(st_dummy[j]));	
				}
				exp.add(new EffluentExperimentalValue(expMassFractions));
				//expMassFractions.clear();
				dummy = in.readLine();

			}
			in.close();
		} catch(IOException e){
			logger.error("Something went wrong during the preprocessing of the experimental data file!",e);
			System.exit(-1);
		}
		
		return exp;
	}
	/**
	 * list of ignition delays is a single column
	 * @param in TODO
	 * @param noIgnitionDelayExperiments
	 * @return
	 * @throws IOException
	 */
	private static List<ExperimentalValue> readExperimentalIgnitionDelays (BufferedReader in){
		List<ExperimentalValue> ignitionDelays = new LinkedList<ExperimentalValue>();
		try {

			String dummy = in.readLine();
			while(dummy!=null){
				ignitionDelays.add(new IgnitionDelayExperimentalValue(Double.parseDouble(dummy)));
				dummy = in.readLine();
			}
			
			in.close();
			return ignitionDelays;
		} catch(IOException e){
			logger.error("Something went wrong during the preprocessing of the Ignition Delay  experimental data file!",e);
			System.exit(-1);
		}
		return null;
	}
	
	private static List<ExperimentalValue> readExperimentalFlameSpeeds(BufferedReader in) {
		List<ExperimentalValue> flameSpeeds = new ArrayList<ExperimentalValue>();
		try {
			//read in species names on first line:
			String dummy = in.readLine();
			while(dummy!=null){
				flameSpeeds.add(new FlameSpeedExperimentalValue(new Double(dummy)));
				//expMassFractions.clear();
				dummy = in.readLine();
			}
			in.close();
			return flameSpeeds;
		} catch(IOException e){
			logger.error("Something went wrong during the preprocessing of the Flame Speed experimental data file!",e);
			System.exit(-1);
		}
		return null;
	}
}
