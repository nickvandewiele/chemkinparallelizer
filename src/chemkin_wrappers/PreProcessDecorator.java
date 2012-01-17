package chemkin_wrappers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import parameter_estimation.ChemkinConstants;


public class PreProcessDecorator extends ChemkinRoutineDecorator {

	AbstractChemkinRoutine routine;

	public PreProcessDecorator(AbstractChemkinRoutine routine){
		this.routine = routine;
	}

	public String[] getKeyword() {
		routine.keywords = new String [3];
		routine.keywords[0] = routine.config.paths.getBinDir()+"CKPreProcess";
		routine.keywords[1] = "-i";
		routine.keywords[2] = routine.config.paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT;
		
		return routine.keywords;
	}

	@Override
	public void executeCKRoutine() {
		this.getKeyword();
		routine.executeCKRoutine();
		
	}
	/**
	 * create CKPreprocess.input file with directions to chem_inp and output/link files of chemistry and transport:
	 * @param out TODO
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void writeCKPreProcessInput()throws IOException, InterruptedException{
		
		System.out.println(config.paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT);
		PrintWriter out = new PrintWriter(new FileWriter(config.paths.getWorkingDir()+ChemkinConstants.PREPROCESSINPUT));
		//in windows: user.dir needs to be followed by "\", in *nix by "/"... 
		String osname = System.getProperty("os.name");
		char osChar;
		if (osname.equals("Linux")){
			osChar = '/';
		}
		else {
			osChar = '\\';
		}		
			out.println("IN_CHEM_INPUT="+config.paths.getWorkingDir()+osChar+config.chemistry.getChemistryInput());
	
			//chemistry output, link and species list:
			out.println("OUT_CHEM_OUTPUT="+config.paths.getWorkingDir()+osChar+ChemkinConstants.CHEMOUT);
			out.println("OUT_CHEM_ASC="+config.paths.getWorkingDir()+osChar+ChemkinConstants.CHEMASC);
			out.println("OUT_CHEM_SPECIES="+config.paths.getWorkingDir()+osChar+ChemkinConstants.CHEMASU);
	
			//transport link files and log file:
			out.println("FIT_TRANSPORT_PROPERTIES=1");
			out.println("OUT_TRAN_OUTPUT="+config.paths.getWorkingDir()+osChar+ChemkinConstants.TRANOUT);
			out.println("OUT_TRAN_ASC="+config.paths.getWorkingDir()+osChar+ChemkinConstants.TRANASC);
	
			out.close();
	}
}
