package chemkin_wrappers;

import java.io.File;
import java.io.IOException;

import readers.ConfigurationInput;

/**
 * Abstract superclass that will commmon information for every chemkin routine that will be called.
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
	
	public abstract String[] getKeyword();
}
