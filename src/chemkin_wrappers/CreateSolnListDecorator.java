package chemkin_wrappers;

import util.ChemkinConstants;

/**
 * Decorator for {@link AbstractChemkinRoutine} that calls the routine "GetSolution" of Chemkin. with the -listonly option.
 * 
 * This option does not retrieve model results but prints the CKSolnList.txt file with flags of which information of the 
 * output that needs to be retrieved.
 * @author Nick
 *
 */
public class CreateSolnListDecorator extends ChemkinRoutineDecorator {

	public CreateSolnListDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	public String[] getKeyword() {
		routine.keywords = new String [3];
		routine.keywords[0] = getConfig().paths.getBinDir()+"GetSolution";
		routine.keywords[1] = "-listonly";
		routine.keywords[2] = getReactorDir()+ChemkinConstants.XML;

		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}

}
