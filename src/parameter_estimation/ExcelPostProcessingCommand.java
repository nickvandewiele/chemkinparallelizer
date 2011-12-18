package parameter_estimation;

import org.apache.log4j.Logger;

/**
 * Command implementation that performs the excel postprocessing
 * option
 * @author nmvdewie
 *
 */
public class ExcelPostProcessingCommand implements Command {
	public static Logger logger = Logger.getLogger(ExcelPostProcessingCommand.class);
	Param_Est par_est;
	
	public ExcelPostProcessingCommand(Param_Est p){
		this.par_est = p;
	}
	public void execute() {
		try {
			logger.info("EXCEL POSTPROCESSING MODE");
			par_est.excelFiles();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
