package levenberg.multi;
//---LMhostmultiD interface class declares five abstract methods---------

interface LMhostmultiD
{
    double dComputeResid() throws Exception;
    // Allows LM to evaluate a starting point. 

    double dNudge(double dp[]) throws Exception;
    // Allows myLM.bLMiter to modify parms[] and reevaluate.
    // This is the only modifier of parms[].
    // So, if NADJ<NPARMS, put your LUT here. 

    boolean bBuildJacobian() throws Exception;
    // Allows LM to request a new Jacobian.

    double dGetResid(int i, int j);
    // Allows LM to access one element of the resid[] vector. 

    double dGetJac(int i, int j, int k);
    // Allows LM to access one element of the Jacobian matrix. 

	boolean bBuildJacobian_forward() throws Exception;

	double[][][] dGetFullJac();
	
}