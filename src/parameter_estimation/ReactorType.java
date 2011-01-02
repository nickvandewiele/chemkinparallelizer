package parameter_estimation;

import java.io.BufferedReader;
import java.io.IOException;


public class ReactorType extends Loggable {
	public String type;
	/*
	 * constants contain values that will be searched for in reactor input file<BR>
	 * depending on the keyword found, one of the different chemkin reactor
	 * routines will be called.
	 */
	public static final String BATCH_REACTOR_TRANSIENT_SOLVER = "TRAN";
	//steady state solver
	public static final String CSTR = "STST";
	public static final String BURNER_STABILIZED_LAMINAR_PREMIXED_FLAME = "BURN";
	public static final String PLUG = "PLUG";
	public static final String FREELY_PROPAGATING_LAMINAR_FLAME = "FREE";

	public ReactorType() {
	}

	/**
	 * To decide which reactortype should be called from the Chemkin library, the first line of each reactor input file
	 * is read.
	 * the first word of this line contains the reactor model.
	 * @param in TODO
	 * @return
	 */
	public String readReactorType(BufferedReader in){
		String reactorType = null;
		try {
			String dummy = in.readLine();
			reactorType = dummy.split(" ")[0];
			in.close();
		} catch (IOException e) {
			CKEmulation.logger.debug(e);
		}
		boolean succes = checkReactorTypeValidity(reactorType);
		if(succes){
			return reactorType;	
		}
		else {
			logger.debug("Reactor Type is not recognized!");
			System.exit(-1);
			return null;
		}
		
	}

	private boolean checkReactorTypeValidity(String reactorType2) {
		boolean validReactorType = reactorType2.equals(BATCH_REACTOR_TRANSIENT_SOLVER)||reactorType2.equals(BURNER_STABILIZED_LAMINAR_PREMIXED_FLAME)||reactorType2.equals(CSTR)||reactorType2.equals(PLUG)||reactorType2.equals(FREELY_PROPAGATING_LAMINAR_FLAME);
		return validReactorType;
	}
}