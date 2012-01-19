package chemkin_wrappers;

import parameter_estimation.AbstractCKEmulation;
import parameter_estimation.ChemkinConstants;

public class CreateSolnListDecorator extends ChemkinRoutineDecorator {
	AbstractChemkinRoutine routine;
	AbstractCKEmulation simulation;

	public CreateSolnListDecorator(AbstractCKEmulation simulation, AbstractChemkinRoutine routine){
		this.simulation = simulation;
		this.routine = routine;
	}
	/**
	 * createSolnList creates the CKSolnList.txt file by calling the "GetSolution -listonly" routine<BR> 
	 */
	public String[] getKeyword() {
		routine.keywords = new String [3];
		routine.keywords[0] = routine.config.paths.getBinDir()+"GetSolution";
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
