package parameter_estimation;

import org.apache.log4j.Logger;

/**
 * Command that executes the statistics calculation via
 * the Param_Est object
 * @author nmvdewie
 *
 */
public class StatisticsCommand implements Command {
	public static Logger logger = Logger.getLogger(StatisticsCommand.class);
	Param_Est par_est;
	
	public StatisticsCommand(Param_Est p){
		this.par_est = p;
	}
	public void execute() {
		logger.info("STATISTICS MODE");
		try {
			par_est.statistics();
			par_est.parity();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
