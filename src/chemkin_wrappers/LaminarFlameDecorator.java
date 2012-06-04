package chemkin_wrappers;


/**
 * Decorator for {@link AbstractChemkinRoutine} that calls the routine "CKReactorFreelyPropagatingFlame" of Chemkin.
 * @author Nick
 *
 */
public class LaminarFlameDecorator extends ChemkinRoutineDecorator {

	public LaminarFlameDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	public String[] getKeyword() {
		routine.keywords = new String [5];
		routine.keywords[0] = getConfig().paths.getBinDir()+"CKReactorFreelyPropagatingFlame";
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
