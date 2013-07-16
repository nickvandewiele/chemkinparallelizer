package commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.apache.log4j.Logger;

import parameter_estimation.CKEmulation;
import parameter_estimation.ChemkinConstants;
import parsers.ConfigurationInput;
import chemkin_wrappers.AbstractChemkinRoutine;
import chemkin_wrappers.ChemkinRoutine;
import chemkin_wrappers.PreProcessDecorator;

public class CheckChemistryFileCommand implements Command {
	public static Logger logger = Logger.getLogger(CheckChemistryFileCommand.class);
	ConfigurationInput config;

	public CheckChemistryFileCommand(ConfigurationInput config){
		this.config = config;
	}
	public void execute() {
		CKEmulation c = new CKEmulation(config);
		//instantiation of parent chemkin routine:
		AbstractChemkinRoutine routine = new ChemkinRoutine(config);
		routine = new PreProcessDecorator(routine);//decoration of parent chemkin routine:
		routine.executeCKRoutine();//execution
		
		c.start();
		try {
			c.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedReader in;
		try {
			File path = new File(config.paths.getWorkingDir(),ChemkinConstants.CHEMOUT);
			in = new BufferedReader(new FileReader(path));
			checkChemOutput(in);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

	}
	/**
	 * checkChemInput does a preliminary check of the initial chemistry output file to verify if no errors are present.<BR>
	 * It calls the Chemkin preprocessor which produces the output file<BR>
	 * This output file is read, and the String  " NO ERRORS FOUND ON INPUT: " is sought.<BR>
	 * If this String is not present, System.exit(-1) is called<BR>
	 * @param in TODO
	 */
	public void checkChemOutput(BufferedReader in){
		try {

			//read the produced chem.out (path_output) file, and check if it contains error messages:
			String dummy = null;
			boolean flag = true;
			try {
				while(flag){
					dummy = in.readLine();
					if (dummy.trim().equals("NO ERRORS FOUND ON INPUT:")){
						flag = false;
					}
				}
				in.close();
				if(!flag){
					logger.info("Initial chemistry input file contains no errors. Proceed to parameter estimation!");
				}

			} catch(Exception e){
				logger.debug("Initial chemistry input file contains errors. Revision required!");
				System.exit(-1);
			}
		}catch(Exception exc){
			logger.error("exception happened - here's what I know: ", exc);
			//exc.printStackTrace();
			System.exit(-1);
		}
	}


}
