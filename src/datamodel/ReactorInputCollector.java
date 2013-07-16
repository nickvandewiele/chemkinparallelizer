package datamodel;

import readers.ReactorSetupInput;
/**
 * Collector for all types of reactor inputs:
 * <LI>-regular (effluent composition)
 * <LI>-ignition delay experiments
 * <LI>-flame speed experiments
 * <BR>
 * both filename and flags are collected here 
 * @author nmvdewie
 *
 */
public class ReactorInputCollector {

	public ReactorSetupInput [] inputs;

	public ReactorInputCollector () {

	}
}