package chemkin_wrappers;

import parameter_estimation.ChemkinConstants;

/**
 * Decorator for {@link AbstractChemkinRoutine} that calls the routine "GetSolution" of Chemkin.
 * 
 * Converts the XMLData.zip file into excel readable csv file with extension .ckcsv.
 * @author Nick
 *
 */
public class GetSolutionDecorator extends ChemkinRoutineDecorator {

	public GetSolutionDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	public String[] getKeyword() {
		//String abbrev_path = cd+"data/abbreviations.csv";
		/**
		 * nosen: no sensitivity data is included
		 * norop: no rate of production info is included
		 */
		routine.keywords = new String [5];
		routine.keywords[0] = getConfig().paths.getBinDir()+"GetSolution";
		routine.keywords[1] = "-norop";
		routine.keywords[2] = "";
		routine.keywords[3] = "";
		routine.keywords[4] = getReactorDir()+ChemkinConstants.XML;

		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();

	}

}
