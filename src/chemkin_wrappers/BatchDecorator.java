package chemkin_wrappers;

import util.Paths;


/**
 * Decorator for {@link AbstractChemkinRoutine} that calls the routine "CKReactorGenericClosed" of Chemkin.
 * 
 * Corresponds to a batch reactor
 * @author Nick
 *
 */
public class BatchDecorator extends ChemkinRoutineDecorator {

	public BatchDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	@Override
	public String[] getKeyword() {
		routine.keywords = new String [5];
		routine.keywords[0] = Paths.getBinDir()+"CKReactorGenericClosed";
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
