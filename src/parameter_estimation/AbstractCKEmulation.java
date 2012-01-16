package parameter_estimation;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import readers.ConfigurationInput;

public abstract class AbstractCKEmulation extends Thread{
	static Logger logger = Logger.getLogger(AbstractCKEmulation.class);

	ConfigurationInput config;

	String reactorDir;

	Runtime runtime;
	boolean flagIgnitionDelayExperiment = true;

	public Experiment experiment;

	protected Effluent effluent;
	protected String reactorSetup;
	protected String reactorOut;
	protected ReactorType reactorType = new ReactorType();

	//'first' is flag that tells you if the CKSolnList needs to be constructed or not.
	boolean first;

	//'excel' tells you if the excel file (transposed CKSoln.ckcsv) needs to be created:
	boolean flagExcel;

	//Semaphore that controls chemkin license check-in and check-outs:
	Semaphore semaphore;
	protected boolean flagFlameSpeedExperiment;

	/**
	 * run() is the method that will be executed when Thread.start() is executed.
	 * Its argument list is void (mandatory I think).
	 */
	public abstract void run();
	

	

}




