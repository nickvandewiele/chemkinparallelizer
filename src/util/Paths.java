package util;

import com.google.common.base.StandardSystemProperty;

/**
 * Paths contains paths to directories, files that are important in the Parameter Estimation program<BR>
 * Paths serves as a supertype to Param_Est, CKPackager, Rosenbrock types
 * @author nmvdewie
 *
 */
public class Paths extends Loggable{
	public static final String workingDir = StandardSystemProperty.USER_DIR.value()
			+StandardSystemProperty.FILE_SEPARATOR.value();
	public static final String outputDir = workingDir+"output/";
	public static String UDROPDir = workingDir+"UDROP/";
	public static String chemkinDir;
	protected static String binDir = chemkinDir+"/bin/";
	public static String chemistryInput;
	public static boolean flagUseMassFractions;
	/**
	 * @category setter
	 * @return
	 */
	public static void setChemkinDir(String cd) {
		chemkinDir = cd;
		setBinDir();
	}
	/**
	 * @category setter
	 * @return
	 */
	private static void setBinDir() {
		binDir = chemkinDir+"/bin/";
	}
	
	

	/**
	 * @category getter
	 * @return
	 */
	public static String getOutputDir() {
		return outputDir;
	}
	/**
	 * @category getter
	 * @return
	 */
	public static String getWorkingDir() {
		return workingDir;
	}
	/**
	 * @category getter
	 * @return
	 */
	public static String getChemkinDir() {
		return chemkinDir;
	}
	/**
	 * @category getter
	 * @return
	 */
	public static String getBinDir() {
		return binDir;
	}
	public static String getUDROPDir() {
		return UDROPDir;
	}
	public static void setConcentration(String data) {
		flagUseMassFractions = data.equals("mass");
		
	}
}
