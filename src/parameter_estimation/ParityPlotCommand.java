package parameter_estimation;

import org.apache.log4j.Logger;

/**
 * Command implementation that performs the parity plot
 * option
 * @author nmvdewie
 *
 */
public class ParityPlotCommand implements Command {
	public static Logger logger = Logger.getLogger(ParityPlotCommand.class);
	Param_Est par_est;
	
	public ParityPlotCommand(Param_Est p){
		this.par_est = p;
	}
	public void execute() {
		logger.info("PARITY PLOT MODE");
		try {
			par_est.parity();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
