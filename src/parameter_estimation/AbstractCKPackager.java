package parameter_estimation;

import readers.ConfigurationInput;

public abstract class AbstractCKPackager extends Loggable{
	
	AbstractCKEmulation [] simulations;
	
	ConfigurationInput config;

	public ModelValue[] modelValues;
	
	public abstract AbstractCKEmulation []  runAllSimulations();
}
