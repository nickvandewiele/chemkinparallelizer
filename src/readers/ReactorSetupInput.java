package readers;

import java.io.File;

/**
 * Type that is used to collect information on reactor input files.
 * @author nmvdewie
 *
 */
public class ReactorSetupInput {
	public static final String AUTO = "AUTO";
	public static final String MANUAL = "MANUAL";
	public enum TYPE {AUTO, MANUAL};
	
	public static final String PFR = "PFR";
	public static final String CSTR = "CSTR";
	public static final String IGNITION_DELAY = "IGNITION_DELAY";
	public static final String FLAME_SPEED = "IGNITION_DELAY";
	
	public enum MODEL {PFR, CSTR, IGNITION_DELAY, FLAME_SPEED};
	
	public String type;
	
	public String model;
	
	/**
	 * location of a single file containing all the information to automatically construct
	 * reactor input files
	 */
	public String location;
	
	/**
	 * a series of user-defined reactor input files
	 */
	public ReactorInput [] user_defined_reactor_inputs;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String string) {
		this.location = string;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}
}
