package chemkin_wrappers;

import util.ChemkinConstants;
import util.Paths;


/**
 * Decorator for {@link AbstractChemkinRoutine} that calls the routine "Chem" of Chemkin.
 * @author Nick
 *
 */
public class ChemDecorator extends ChemkinRoutineDecorator {

	public ChemDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	@Override
	public String[] getKeyword() {
		
		routine.keywords = new String [7];
		routine.keywords[0] = Paths.getBinDir()+"chem";
		routine.keywords[1] = "-i";
		routine.keywords[2] = Paths.chemistryInput;
		routine.keywords[3] = "-o";
		routine.keywords[4] = ChemkinConstants.CHEMOUT;
		routine.keywords[5] = "-c";
		routine.keywords[6] = ChemkinConstants.CHEMASC;
		return routine.keywords;

	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}

}
