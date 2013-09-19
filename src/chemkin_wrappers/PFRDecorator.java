package chemkin_wrappers;


/**
 * Decorator for {@link AbstractChemkinRoutine} that calls the routine "CKReactorPlugFlow" of Chemkin.
 * 
 * Corresponds to PFR model.
 * @author Nick
 *
 */
public class PFRDecorator extends ChemkinRoutineDecorator {


	public PFRDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	@Override
	public String[] getKeyword() {
		
		routine.keywords = new String [5];
		routine.keywords[0] = getConfig().paths.getBinDir()+"CKReactorPlugFlow";
		routine.keywords[1] = "-i";
		routine.keywords[2] = getReactorDir()+getReactorSetup();
		routine.keywords[3] = "-o";
		routine.keywords[4] = getReactorDir()+getReactorOut();
	
		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}


}
