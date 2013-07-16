package datamodel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;

import util.Loggable;

/**
 * collects response variables, which can not only be species names,
 * but also ignition delays, flame speeds etc <BR>
 * for the latter a boolean value is kept.
 * @author nmvdewie
 *
 */
public class ResponseVariables extends Loggable {
	LinkedList<String> effluentResponses;
	boolean ignitionDelay;
	boolean flameSpeed;
	
	public ResponseVariables(){
		this.effluentResponses = new LinkedList<String>();
		this.ignitionDelay = false;
		this.flameSpeed = false;
		
	}
	
	public void printResponseVariables(PrintWriter out) throws IOException{
		//PrintWriter out_species = new PrintWriter(new FileWriter("response_vars.txt"));
		//print effluent responses:
		for(Iterator<String> it = effluentResponses.iterator(); it.hasNext();){
			out.println((String)it.next());
		}
		
		//print ignition delay:
		if(ignitionDelay) out.println("Ignition Delay");
		
		//print flame speed:
		if(flameSpeed) out.println("Flame Speed");
	}
	
	public int getNoResponses(){
		return calcNoResponses();
	}
	
	private int calcNoResponses() {
		int number = 0;
		number += effluentResponses.size();
		if(ignitionDelay) number++;
		if(flameSpeed) number++;
		return number;
	}

	public LinkedList<String> getEffluentResponses() {
		return effluentResponses;
	}
	public void setEffluentResponses(LinkedList<String> effluentResponses) {
		this.effluentResponses = effluentResponses;
	}
	public boolean isIgnitionDelay() {
		return ignitionDelay;
	}
	public void setIgnitionDelay(boolean ignitionDelay) {
		this.ignitionDelay = ignitionDelay;
	}
	public boolean isFlameSpeed() {
		return flameSpeed;
	}
	public void setFlameSpeed(boolean flameSpeed) {
		this.flameSpeed = flameSpeed;
	}

	public LinkedList<String> readEffluentResponses(BufferedReader in){
		LinkedList<String> effluentResponses = new LinkedList<String>();
		String dummy;
		try {
			dummy = in.readLine();
			String[] speciesArray = dummy.split(",");
			for(int i = 0; i < speciesArray.length; i++){
				effluentResponses.add(speciesArray[i]);
			}
			in.close();
	
		} catch (IOException e) {
		}
		return effluentResponses;						
	}
}
