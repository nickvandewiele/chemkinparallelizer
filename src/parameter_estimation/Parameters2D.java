package parameter_estimation;
/**
 * Parameters2D stores information about the parameters in 2D format, as obtained from the user (chemInp, INPUT.txt)
 * @author nmvdewie
 *
 */
public class Parameters2D {
	private double [][] beta;
	private double [][] betaMin;
	private double [][] betaMax;
	private  int [][] fixRxns;
	private int noFittedParameters;
	public int getNoFittedParameters() {
		noFittedParameters = calcNoFittedParamaters();
		return noFittedParameters;
	}

	public Parameters2D(int [][] fixRxns){
		this.fixRxns = fixRxns;
	}
	public Parameters2D (double [][] beta, double [][] betamin, double [][] betamax, int [][] fixRxns){
		this.beta = beta;
		this.betaMin = betamin;
		this.betaMax = betamax;
		this.fixRxns = fixRxns;
	}
	/**
	 * @category getter
	 * @return
	 */
	public double[][] getBeta() {
		return beta;
	}
	/**
	 * @category getter
	 * @return
	 */
	public double[][] getBetamin() {
		return betaMin;
	}
	/**
	 * @category getter
	 * @return
	 */
	public double[][] getBetamax() {
		return betaMax;
	}
	/**
	 * @category getter
	 * @return
	 */
	public int[][] getFixRxns() {
		return fixRxns;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setBeta(double[][] beta) {
		this.beta = beta;
	}
	/**
	 * @category getter
	 * @return
	 */
	public double[][] getBetaMin() {
		return betaMin;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setBetaMin(double[][] betaMin) {
		this.betaMin = betaMin;
	}
	/**
	 * @category getter
	 * @return
	 */
	public double[][] getBetaMax() {
		return betaMax;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setBetaMax(double[][] betaMax) {
		this.betaMax = betaMax;
	}
	/**
	 * @category setter
	 * @return
	 */
	public void setFixRxns(int[][] fixRxns) {
		this.fixRxns = fixRxns;
	}
	private int calcNoFittedParamaters(){
		int counter = 0;
		for(int i = 0; i < fixRxns.length; i++){
			for (int j = 0; j < fixRxns[0].length; j++){
				counter += fixRxns[i][j];
			}
		}
		return counter;
	}
}
