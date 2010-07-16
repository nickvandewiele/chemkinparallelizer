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
	public Parameters2D (double [][] beta, double [][] betamin, double [][] betamax, int [][] fixRxns){
		this.beta = beta;
		this.betaMin = betamin;
		this.betaMax = betamax;
		this.fixRxns = fixRxns;
	}
	public double[][] getBeta() {
		return beta;
	}
	public double[][] getBetamin() {
		return betaMin;
	}
	public double[][] getBetamax() {
		return betaMax;
	}
	public int[][] getFixRxns() {
		return fixRxns;
	}
	public void setBeta(double[][] beta) {
		this.beta = beta;
	}
}
