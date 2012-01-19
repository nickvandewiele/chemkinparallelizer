package chemkin_wrappers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import parameter_estimation.ChemkinConstants;


public class PreProcessDecorator extends ChemkinRoutineDecorator {


	public PreProcessDecorator(AbstractChemkinRoutine routine){
		super.routine = routine;
	}

	public String[] getKeyword() {
		routine.keywords = new String [3];
		routine.keywords[0] = getConfig().paths.getBinDir()+"CKPreProcess";
		routine.keywords[1] = "-i";
		routine.keywords[2] = routine.config.paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT;

		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		writeCKPreProcessInput();
		routine.executeCKRoutine();

	}
	/**
	 * create CKPreprocess.input file with directions to chem_inp and output/link files of chemistry and transport:
	 * @param out TODO
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void writeCKPreProcessInput(){

		System.out.println(routine.config.paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT);
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter(routine.config.paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT));
			//in windows: user.dir needs to be followed by "\", in *nix by "/"... 
			String osname = System.getProperty("os.name");
			char osChar;
			if (osname.equals("Linux")){
				osChar = '/';
			}
			else {
				osChar = '\\';
			}		
			out.println("IN_CHEM_INPUT="+routine.config.paths.getWorkingDir()+osChar+routine.config.chemistry.getChemistryInput());

			//chemistry output, link and species list:
			out.println("OUT_CHEM_OUTPUT="+routine.config.paths.getWorkingDir()+osChar+ChemkinConstants.CHEMOUT);
			out.println("OUT_CHEM_ASC="+routine.config.paths.getWorkingDir()+osChar+ChemkinConstants.CHEMASC);
			out.println("OUT_CHEM_SPECIES="+routine.config.paths.getWorkingDir()+osChar+ChemkinConstants.CHEMASU);

			//transport link files and log file:
			out.println("FIT_TRANSPORT_PROPERTIES=1");
			out.println("OUT_TRAN_OUTPUT="+routine.config.paths.getWorkingDir()+osChar+ChemkinConstants.TRANOUT);
			out.println("OUT_TRAN_ASC="+routine.config.paths.getWorkingDir()+osChar+ChemkinConstants.TRANASC);

			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
