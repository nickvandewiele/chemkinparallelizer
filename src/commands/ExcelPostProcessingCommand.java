package commands;

import org.apache.log4j.Logger;

import chemkin_model.AbstractCKPackager;
import chemkin_model.CKPackager;

import util.Tools;

/**
 * Command implementation that performs the excel postprocessing
 * option
 * @author nmvdewie
 *
 */
public class ExcelPostProcessingCommand implements Command {
	public static Logger logger = Logger.getLogger(ExcelPostProcessingCommand.class);
	public ExcelPostProcessingCommand(){
	}
	public void execute() {
		try {
			logger.info("EXCEL POSTPROCESSING MODE");
			excelFiles();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	/**
	 * this routine produces model predictions without comparing them to experimental data
	 * @throws Exception 
	 * @throws Exception 
	 */
	public void excelFiles() throws Exception{
		long time = System.currentTimeMillis();
		//check if initial input file is error-free:
		Command checkChemistry = new CheckChemistryFileCommand();
		checkChemistry.execute();

		AbstractCKPackager ckp = new CKPackager();
		ckp.runAllSimulations();
		
		Tools.moveOutputFiles();
		long timeTook = (System.currentTimeMillis() - time)/1000;
		logger.info("Time needed for Excel Postprocessing mode to finish: (sec) "+timeTook);
	}

}
