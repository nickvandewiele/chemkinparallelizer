package readers;

/**
 * Class that contains information on how to simulate a specific reactor.
 * 
 * 
 * @author nmvdewie
 *
 */
public class ReactorInput {
	public static final String PFR = "PFR";
	public static final String CSTR = "CSTR";
	public static final String IGNITION_DELAY = "IGNITION_DELAY";
	public static final String FLAME_SPEED = "FLAME_SPEED";
	
	public String type;
	
	public String filename;//create reactor input file
	
	public ReactorInput(String type, String filename){
		this.type = type;
		this.filename = filename;
	}
}
