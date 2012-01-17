package datatypes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import datatypes.ModelValue.TYPE;


public class IgnitionDelayModelValue extends ModelValue {

	
	TYPE type = ModelValue.TYPE.IGNITION_DELAY;
	
	public double value;

	@Override
	public void setValue(BufferedReader bufferedReader) {
		readCkcsvIgnitionDelay(bufferedReader);
		
	}
	
	/**
	 * will search for String "Ignition_time_1_by_max_dT/dt" and return value of it
	 * @param in TODO
	 * @return
	 */
	public void readCkcsvIgnitionDelay(BufferedReader in) {
		readCkcsv(in, "Ignition_time_1_by_max_dT/dt");
		
	}
	
	public void readCkcsv(BufferedReader in, String CkcsvType){
		
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
					
				}
			} while (!(list_temp.get(0)).equals(CkcsvType));
			in.close();
			//take last value in row:
			value = new Double(list_temp.get(list_temp.size()-1));
			
		} catch (FileNotFoundException e) {
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			

	}

	@Override
	public double getSSQValue() {
		return Math.pow(value, 2);
	}
}
