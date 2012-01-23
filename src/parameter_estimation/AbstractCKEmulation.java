package parameter_estimation;

import org.apache.log4j.Logger;

import parsers.ConfigurationInput;

import datatypes.ExperimentalValue;
import datatypes.ModelValue;

import readers.ReactorInput;

public abstract class AbstractCKEmulation extends Thread{
	static Logger logger = Logger.getLogger(AbstractCKEmulation.class);

	ConfigurationInput config;

	Runtime runtime;

	public ExperimentalValue experiment;

	protected ModelValue modelValue;
	
	public String reactorDir;
	
	public ReactorInput reactorInput;
	
	protected String reactorOut;

	//Semaphore that controls chemkin license check-in and check-outs:
	Semaphore semaphore;


	/**
	 * run() is the method that will be executed when Thread.start() is executed.
	 * Its argument list is void (mandatory I think).
	 */
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

	public Runtime getRuntime() {
		return runtime;
	}

	public ModelValue getModelValue() {
		return modelValue;
	}
}




