package chemkin_model;

import org.apache.log4j.Logger;

import parsers.ConfigurationInput;
import readers.ReactorInput;
import datamodel.ExperimentalValue;
import datamodel.ModelValue;

/**
 * Abstract class that represents a chemkin simulation.
 * 
 * It takes in a reactor input file, runs the necessary chemkin routines and stores the model results 
 * that are associated with the chemkin simulation and that are wanted by the user.
 * 
 * Attributes are:
 * <LI> Configuration file
 * <LI> pointers to directories
 * <LI> a model value object
 * <LI> a experimental value object
 * 
 * Since this type is the base for decoration, getters are provided for all of the attributes.
 * 
 * An abstract run() method declares the procedure to run a simulation.
 * 
 * This type is extending the {@link Thread} type that allows multithreading.
 * @author Nick
 *
 */
public abstract class AbstractCKEmulation extends Thread{
	static Logger logger = Logger.getLogger(AbstractCKEmulation.class);

	ConfigurationInput config;

	public ExperimentalValue experiment;

	protected ModelValue modelValue;
	
	public String reactorDir;
	
	public ReactorInput reactorInput;
	
	protected String reactorOut;

	/**
	 * run() is the method that will be executed when Thread.start() is executed.
	 * Its argument list is void (mandatory I think).
	 */
	@Override
	public abstract void run();

	public ConfigurationInput getConfig(){
		return config;
	}

	public String getReactorDir() {
		return reactorDir;
	}

	public ReactorInput getReactorInput() {
		return reactorInput;
	}

	public String getReactorOut() {
		return reactorOut;
	}

	public ModelValue getModelValue() {
		return modelValue;
	}
}




