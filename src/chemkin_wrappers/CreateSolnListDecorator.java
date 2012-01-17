package chemkin_wrappers;

import parameter_estimation.ChemkinConstants;

public class CreateSolnListDecorator extends ChemkinRoutineDecorator {
	AbstractChemkinRoutine routine;

	public CreateSolnListDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}
	/**
	 * createSolnList creates the CKSolnList.txt file by calling the "GetSolution -listonly" routine<BR> 
	 */
	public String[] getKeyword() {
		routine.keywords = new String [3];
		routine.keywords[0] = config.paths.getBinDir()+"GetSolution";
		routine.keywords[1] = "-listonly";
		routine.keywords[2] = reactorDir+ChemkinConstants.XML;

		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}

}
