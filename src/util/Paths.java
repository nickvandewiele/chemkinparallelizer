package util;

import java.io.File;
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
	//folder where ParameterEstimationDriver is located
	public static File EXEC_LOCATION;
	
	public static final String outputDir = workingDir+"output/";
	protected static File binDir = new File(System.getenv("CHEMKIN"),"bin");
	public static String chemistryInput;
	public static boolean flagUseMassFractions;	

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
	public static String getBinDirLocation() {
		return binDir.getAbsolutePath()+StandardSystemProperty.FILE_SEPARATOR.value();
	}
	public static String getUDROPDir() {
		return new File(EXEC_LOCATION,"UDROP").getAbsolutePath();
	}
	public static void setConcentration(String data) {
		flagUseMassFractions = data.equals("mass");
		
	}
	public static void setJarLocation(File file) {
		EXEC_LOCATION = file;
		
	}
}
