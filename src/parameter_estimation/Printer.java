package parameter_estimation;

import java.io.PrintWriter;

import org.apache.log4j.Logger;

import parameter_estimation.levenberg.LM_NBMT;

public class Printer {
	public static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
	public static void printArray(double [] d, PrintWriter out){
		for (int i = 0; i < d.length; i++) {
			out.print(d[i]+" ");
			logger.info(d[i]+" ");
		}
		out.println();
		//System.out.println();
	}
	public static void printMatrix(double [][] d,PrintWriter out){
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				out.print(d[i][j]+" ");
				logger.info(d[i][j]+" ");			
			}
			out.println();
	    	logger.info(" ");
		}
		out.println();
		//System.out.println();
	}
	public void print3DMatrix(double [][][] d,PrintWriter out){
		for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				for (int k = 0; k < d[0][0].length; k++) {
					out.print(d[i][j][k]+" ");
					logger.info(d[i][j][k]+" ");			
				}
				out.println();
		    	logger.info("");
			}
			out.println();
	    	logger.info("");
		}
		out.println();
		//System.out.println();
	}
}
