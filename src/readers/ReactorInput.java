package readers;

import java.io.File;

/**
 * Class that contains information on how to simulate a specific reactor.
 * 
 * 
 * @author nmvdewie
 *
 */
public class ReactorInput {
	public enum REACTORTYPE {PFR, CSTR, IGNITION_DELAY, FLAME_SPEED}
	
	public String type;
	
	public File filename;//create reactor input file
}
