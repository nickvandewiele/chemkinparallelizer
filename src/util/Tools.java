package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
/**
 * Tools provides static methods to move/delete one File or multiple files with the same extension
 * @author nmvdewie
 *
 */
public class Tools extends Loggable {
	/**
	 * moveFiles moves all files with extension e to the specified destination dir
	 * @param orig_dir
	 * @param dest_dir
	 * @param e
	 */
	public static void moveFiles( String orig_dir, String dest_dir, String e ) {
		ExtensionFilter filter = new ExtensionFilter(e);
		File original_dir = new File(orig_dir);
		String[] list = original_dir.list(filter);
		if (list.length == 0) return;

		for (int i = 0; i < list.length; i++) {
			// Move file to new directory
			File file = new File(list[i]);
			boolean success = file.renameTo(new File(dest_dir, list[i]));
			if (success) logger.info("File was successfully moved!"); 
			else logger.debug("File was not successfully moved...");
			//logger.info("File was successfully moved? " + success + "!");
		}    
	}

	public static void moveFile(String dest_dir, String filename){
		// File (or directory) to be moved
		File file = new File(filename);

		// Move file to new directory
		boolean success = file.renameTo(new File(dest_dir, file.getName()));
		if (!success) {
			// File was not successfully moved
		}
	}
	/**
	 * convert 1D vector into 2D matrix: 
	 * @return
	 */
	public static double [][] convert1Dto2D(double [] vector, double[][] matrix){
		//convert 1D vector back to matrix [][] notation:
		double [][] matrixFilled = new double [matrix.length][matrix[0].length];
		int counter = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++){
				matrixFilled[i][j] = vector[counter];
				counter++;
			}
		}
		return matrixFilled;
	}
	/**
	 * convert_massfractions cuts of the Mass_fraction_ part off of the species names string
	 * @param m
	 * @return
	 */
	public static Map<String,Double> cutOffMassFrac_(Map<String,Double> m){
		Map<String, Double> dummy = new HashMap<String, Double> ();
		//loop through keys
		for ( String s : m.keySet()){
			if(s.contains("fraction_")){
				//omit substring "M***_fraction_" from key, i.e. take substring starting from character at position 14
				String dummy_name = s.substring(14);
				Double dummy_value = m.get(s);
				dummy.put(dummy_name, dummy_value);
			}
			else dummy.put(s, m.get(s));
		}
		return dummy;
	}	
	public static void moveOutputFiles (){
		Tools.moveFiles(Paths.getWorkingDir(), Paths.getOutputDir(), ".out");
		Tools.moveFiles(Paths.getWorkingDir(), Paths.getOutputDir(), ".asu");
		Tools.moveFiles(Paths.getWorkingDir(), Paths.getOutputDir(), ".input");
		Tools.moveFiles(Paths.getWorkingDir(), Paths.getOutputDir(), ".asc");
		Tools.moveFile(Paths.getOutputDir(),"SpeciesParity.csv");
		Tools.moveFile(Paths.getOutputDir(),"IgnitionDelayParity.csv");
		Tools.moveFile(Paths.getOutputDir(),"FlameSpeedParity.csv");
		Tools.moveFile(Paths.getOutputDir(),"CKSolnList.txt");

	}
}
