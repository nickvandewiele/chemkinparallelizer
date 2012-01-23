package chemkin_wrappers;

import parsers.ConfigurationInput;


/**
 * Abstract superclass that will commmon information for every chemkin routine that will be called.
 * 
 * Attributes are:
 * <LI> the Configuration object
 * <LI> pointers to reactor directory, reactor setup, reactor output file
 * <LI> a runtime object
 * <LI> a String array containing the keywords that will be passed as command line arguments.
 * 
 * A method {@link AbstractChemkinRoutine} executeCKRoutine() executes the keyword array.
 * 
 * For each of the attributes getters are provided and will be called by the decorators.
 * @author nmvdewie
 *
 */
public abstract class AbstractChemkinRoutine {
	
	public ConfigurationInput config;
	
	public String reactorDir;//name of dir in which the routine will be executed
	
	public String reactorSetup;//name of reactor setup file
	
	public String reactorOut;//name of simulation output file.
	
	Runtime runtime;
	
	public abstract void executeCKRoutine();
	
	public String [] keywords;

	public String getReactorDir() {
		return reactorDir;
	}

	public String getReactorSetup() {
		return reactorSetup;
	}

	public String getReactorOut() {
		return reactorOut;
	}

	public ConfigurationInput getConfig() {
		return config;
	}

	public Runtime getRuntime() {
		return runtime;
	}
}
