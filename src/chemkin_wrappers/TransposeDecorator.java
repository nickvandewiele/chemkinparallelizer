package chemkin_wrappers;

import util.ChemkinConstants;


/**
 * Decorator for {@link AbstractChemkinRoutine} that calls the routine "CKSolnTranspose" of Chemkin.
 * 
 * The transpose utility converts the .ckcsv output of GetSolution into the transposed.
 * @author Nick
 *
 */
public class TransposeDecorator extends ChemkinRoutineDecorator {

	public TransposeDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	@Override
	public String[] getKeyword() {
		routine.keywords = new String [3];
		routine.keywords[0] = getConfig().paths.getBinDir()+"CKSolnTranspose";
		routine.keywords[1] = "-i";
		routine.keywords[2] = getConfig().paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT;

		return routine.keywords;

	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}

}
