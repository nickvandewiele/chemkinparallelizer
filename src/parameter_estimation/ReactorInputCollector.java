package parameter_estimation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
/**
 * Collector for all types of reactor inputs:
 * <LI>-regular (effluent composition)
 * <LI>-ignition delay experiments
 * <LI>-flame speed experiments
 * <BR>
 * both filename and flags are collected here 
 * @author nmvdewie
 *
 */
public class ReactorInputCollector {
	LinkedList<String> reactorInputs = new LinkedList<String>();
	
	LinkedList<String> regularInputs = new LinkedList<String>();
	LinkedList<String> ignitionDelayInputs = new LinkedList<String>();
	LinkedList<String> flameSpeedInputs = new LinkedList<String>();
	
	private Integer noRegularExperiments;
	private Integer noIgnitionDelayExperiments;
	private Integer noFlameSpeedExperiments;
	
	private Integer totalNoExperiments;
	
	/**
	 * 
	 * flags to indicate which reactor input files are dedicated to ignition delay experiments
	 * the mapping is between the filename and a boolean
	 * true = ignition delay experiment
	 * false = other type of experiment
	 */
	private Map<String,Boolean> flagIgnitionDelays = new HashMap<String,Boolean>();
	
	/**
	 * 
	 * flags to indicate which reactor input files are dedicated to flame speed experiments
	 * the mapping is between the filename and a boolean
	 * true = flame speed experiment
	 * false = other type of experiment
	 */
	private Map<String,Boolean> flagFlameSpeedDelays = new HashMap<String,Boolean>();
	


	public ReactorInputCollector () {
		
	}
	
	/**
	 * merges RegularReactorInputs with IgnitionDelayInputs and FlameSpeedInputs
	 * if both regular reactor inputs and ignition delay inputs are present,
	 * <LI>first the regular reactor inputs will be parsed into ReactorInputs
	 * <LI>then the ignition delay reactor inputs
	 * <LI>then the flame speed reactor inputs
	 * @return
	 */
	public void mergeReactorInputs(){
		if(noRegularExperiments==0){
			if(noIgnitionDelayExperiments == 0){
				reactorInputs.addAll(flameSpeedInputs);
			}
			else {
				reactorInputs.addAll(ignitionDelayInputs);
				reactorInputs.addAll(flameSpeedInputs);
			}
		}
		else{
			reactorInputs.addAll(regularInputs);
			reactorInputs.addAll(ignitionDelayInputs);
			reactorInputs.addAll(flameSpeedInputs);
			
		}
	}
	
	public void setFlagIgnitionDelays() {
		//flag true for ignition delays:
		for (int i = 0; i < noIgnitionDelayExperiments; i++){			
			//mark that these are ignition delay experiments:
			flagIgnitionDelays.put(ignitionDelayInputs.get(i),true);
		}
		//flag false for regular experiments:
		for (int i = 0; i < noRegularExperiments; i++){			
			//mark that these are ignition delay experiments:
			flagIgnitionDelays.put(regularInputs.get(i),false);
		}
		//flag false for flame speed experiments:
		for (int i = 0; i < noFlameSpeedExperiments; i++){			
			//mark that these are ignition delay experiments:
			flagIgnitionDelays.put(flameSpeedInputs.get(i),false);
		}
	}
	
	public void setFlagFlameSpeeds() {
		//flag true for flame speeds:
		for (int i = 0; i < noFlameSpeedExperiments; i++){			
			//mark that these are ignition delay experiments:
			flagFlameSpeedDelays.put(flameSpeedInputs.get(i),true);
		}
		//flag false for regular experiments:
		for (int i = 0; i < noRegularExperiments; i++){			
			//mark that these are ignition delay experiments:
			flagFlameSpeedDelays.put(regularInputs.get(i),false);
		}
		//flag false for ignition delay experiments:
		for (int i = 0; i < noFlameSpeedExperiments; i++){			
			//mark that these are ignition delay experiments:
			flagFlameSpeedDelays.put(ignitionDelayInputs.get(i),false);
		}
	}
	
	
	/**
	 * ####################
	 * GETTERS AND SETTERS:
	 * ####################
	 */
	/**
	 * @category getter
	 * @return
	 */
	public Integer getNoIgnitionDelayExperiments() {
		return noIgnitionDelayExperiments;
	}
	/**
	 * @category getter
	 * @return
	 */
	public Integer getTotalNoExperiments() {
		return totalNoExperiments;
	}
	/**
	 * @category getter
	 * @return
	 */
	public Integer getNoRegularExperiments() {
		return totalNoExperiments - noIgnitionDelayExperiments;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setIgnitionDelayInputs(LinkedList<String> ignitionDelayInputs) {
		this.ignitionDelayInputs = ignitionDelayInputs;
	}
	/**
	 * @category getter
	 * @param ignitionDelayInputs
	 */
	public LinkedList<String> getRegularInputs() {
		return regularInputs;
	}
	/**
	 * @category setter
	 * @param ignitionDelayInputs
	 */
	public void setRegularInputs(LinkedList<String> regularInputs) {
		this.regularInputs = regularInputs;
	}
	
	/**
	 * @category getter
	 * @return
	 */
	public LinkedList<String> getFlameSpeedInputs() {
		return flameSpeedInputs;
	}
	/**
	 * @category setter
	 * @param flameSpeedInputs
	 */
	public void setFlameSpeedInputs(LinkedList<String> flameSpeedInputs) {
		this.flameSpeedInputs = flameSpeedInputs;
	}
	/**
	 * @category getter
	 * @param ignitionDelayInputs
	 */
	public LinkedList<String> getIgnitionDelayInputs() {
		return ignitionDelayInputs;
	}
	/**
	 * @category setter
	 */
	public void setTotalNoExperiments(Integer totalNoExperiments) {
		this.totalNoExperiments = totalNoExperiments;
	}
	/**
	 * @category setter
	 */
	public void setNoIgnitionDelayExperiments(Integer number) {
		noIgnitionDelayExperiments = number;
		
	}
	/**
	 * @category getter
	 * @return
	 */
	public Integer getNoFlameSpeedExperiments() {
		return noFlameSpeedExperiments;
	}
	/**
	 * @category setter
	 * @param i
	 */
	public void setNoRegularExperiments(int i) {
		noRegularExperiments = i;
		
	}
	/**
	 * @category getter
	 * @return
	 */
	public LinkedList<String> getReactorInputs() {
		return reactorInputs;
	}
	/**
	 * @category setter
	 * @param reactorInputs
	 */
	public void setReactorInputs(LinkedList<String> reactorInputs) {
		this.reactorInputs = reactorInputs;
	}
	/**
	 * @category getter
	 * @return
	 */
	public Map<String, Boolean> getFlagIgnitionDelays() {
		return flagIgnitionDelays;
	}
	/**
	 * @category getter
	 * @return
	 */
	public Map<String, Boolean> getFlagFlameSpeedDelays() {
		return flagFlameSpeedDelays;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setFlagFlameSpeedDelays(Map<String, Boolean> flagFlameSpeedDelays) {
		this.flagFlameSpeedDelays = flagFlameSpeedDelays;
	}
	
	/**
	 * @category setter
	 * @return
	 */
	public void setNoFlameSpeedExperiments(Integer noFlameSpeedExperiments) {
		this.noFlameSpeedExperiments = noFlameSpeedExperiments;
	}
	
	
}
