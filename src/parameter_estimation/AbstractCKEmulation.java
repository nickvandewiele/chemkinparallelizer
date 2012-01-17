package parameter_estimation;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import parsers.ConfigurationInput;

import datatypes.ExperimentalValue;
import datatypes.ModelValue;

import readers.ReactorSetupInput;

public abstract class AbstractCKEmulation extends Thread{
	static Logger logger = Logger.getLogger(AbstractCKEmulation.class);

	ConfigurationInput config;

	String reactorDir;

	Runtime runtime;

	public ExperimentalValue experiment;

	protected ModelValue modelValue;
	
	ReactorSetupInput reactorSetupInput;
	
	protected String reactorOut;

	//Semaphore that controls chemkin license check-in and check-outs:
	Semaphore semaphore;


	/**
	 * run() is the method that will be executed when Thread.start() is executed.
	 * Its argument list is void (mandatory I think).
	 */
	public abstract void run();

}




