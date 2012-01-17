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
			if(input.type.equals(ReactorSetupInput.TYPE.AUTO)){
				ReactorInputParsable parser;
				parser = new PFRReactorInputParser1(config.paths.getWorkingDir()+input.location);		
				parser.parse();

			}
		}
	}
}
