package chemkin_wrappers;

import util.ChemkinConstants;
import util.Paths;


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
		routine.keywords[0] = Paths.getBinDirLocation()+"CKSolnTranspose";
		routine.keywords[1] = "-i";
		routine.keywords[2] = Paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT;

		return routine.keywords;

	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}

}
