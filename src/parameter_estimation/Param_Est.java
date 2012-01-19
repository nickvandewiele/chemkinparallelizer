package parameter_estimation;
import parsers.ConfigurationInput;
import parsers.PFRReactorInputParser1;
import parsers.ReactorInputParsable;
import readers.ReactorSetupInput;


public class Param_Est extends Loggable{

	ConfigurationInput config;

	public Param_Est(ConfigurationInput config) {
		this.config = config;

		/**
		 * create reactor input files, if necessary:
		 */
		for(ReactorSetupInput input : config.reactor_setup){
			config.addReactorInput(input);
		}
	}
}
