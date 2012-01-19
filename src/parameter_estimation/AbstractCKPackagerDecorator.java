package parameter_estimation;

import datatypes.ModelValue;
import parsers.ConfigurationInput;

public abstract class AbstractCKPackagerDecorator extends AbstractCKPackager {

	AbstractCKPackager packager;
	
	public ConfigurationInput getConfig() {
		return packager.getConfig();
	}
	
	public ModelValue[] getModelValues() {
		return packager.getModelValues();
	}
}
