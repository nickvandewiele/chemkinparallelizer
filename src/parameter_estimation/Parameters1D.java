package parameter_estimation;
/**
 * Parameters1D stores the parameters in 1D (Vector) format, as requested for the optimization routines 
 * @author nmvdewie
 *
 */
public class Parameters1D {
	private double [] beta;
	private double [] betaMin;
	private double [] betaMax;
	private  int [] fixRxns;
	public Parameters1D (){
	}
	public double[] getBeta() {
		return beta;
	}
	public double[] getBetamin() {
		return betaMin;
	}
	public double[] getBetamax() {
		return betaMax;
	}
	public int[] getFixRxns() {
		return fixRxns;
	}
	public void setBeta(double[] beta) {
		this.beta = beta;
	}
	/**
	 * convert2D_to_1D() will initialize the 1D vectors derived from the 2D matrices containing parameter data to be used in optimization routine
	 */
	public void convert2Dto1D(Parameters2D params2D){
		//for programming ease of the optimization algorithm, the double [][] matrix b_old (containing parameters per reaction)
		//will be converted to a 1D vector:	
		int totalNoParameters = params2D.getFixRxns().length * params2D.getFixRxns()[0].length;
	
		beta = new double[totalNoParameters];

		betaMin = new double[totalNoParameters];
		betaMax = new double[totalNoParameters];
		fixRxns = new int[totalNoParameters];
			
		//copy values of [][] matrices to [] vectors:
		int counter = 0;
		for (int i = 0; i < params2D.getFixRxns().length; i++) {
			for (int j = 0; j < params2D.getFixRxns()[0].length; j++){
				beta[counter] = params2D.getBeta()[i][j];
				betaMin[counter] = params2D.getBetamin()[i][j];
				betaMax[counter] = params2D.getBetamax()[i][j];
				fixRxns[counter] = params2D.getFixRxns()[i][j];				
				counter++;
			}
		}
	}	
}
