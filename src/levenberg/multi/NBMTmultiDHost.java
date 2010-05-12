package levenberg.multi;

import parameter_estimation.Function;
import parameter_estimation.Optimization;

public class NBMTmultiDHost implements LMhostmultiD {

	//--------constants---------------
    protected final double DELTAP = 1e-6;
    protected final double BIGVAL = 9.876543E+210; 
    protected int NPTS, NPARMS, NRESP, NEXPL; //modified by constructor

    //--------fields------------------
    
    private double resid[][]; //residual matrix Y - Y^ with rows as experiments
    private double jac[][][];  
    private double parms[];  // starting point
    
    private Optimization optimization;
    
    public NBMTmultiDHost(Optimization o) throws Exception
    {
    	this.optimization = o;
    	this.parms = optimization.retrieve_fitted_parameters();
    	
    	NPARMS = parms.length;
    	NPTS = (optimization.getExp()).size();
    	NRESP = (optimization.getExp().get(0)).size();
    	
    	resid = new double[NPTS][NRESP];
    	jac = new double[NPTS][NPARMS][NRESP];
    	
    	for (int i=0; i<NPARMS; i++)
          System.out.println("Start parm["+i+"] = "+parms[i]); 

        LMmultiD myLM = new LMmultiD(this, NPARMS, NPTS, NRESP); // run the minimizer

        for (int i=0; i<NPARMS; i++)
          System.out.println("End parm["+i+"]   = "+parms[i]); 
    }

	public boolean bBuildJacobian() throws Exception {
		// Allows LM to compute a new Jacobian.
	    // Uses current parms[] and two-sided finite difference.
	    // If current parms[] is bad, returns false.  
	    
	        double delta[] = new double[NPARMS];
	        double FACTOR = 0.5 / DELTAP; 
	        double d=0; 

	        for (int j=0; j<NPARMS; j++)
	        {
	            for (int k=0; k<NPARMS; k++)
	            	//previously:delta[k] = (k==j) ? DELTAP : 0.0;
	              delta[k] = (k==j) ? parms[k] * DELTAP : 0.0;

	            d = dNudge(delta); // resid at pplus
	            if (d==BIGVAL)
	            {
	                System.out.println("Bad dBuildJacobian() exit 2"); 
	                return false;  
	            }
	            for (int i=0; i<NPTS; i++){
	            	for (int k = 0; k < NRESP; k++){
	            		jac[i][j][k] = dGetResid(i,k);	
	            	}
	            } 

	            for (int k=0; k<NPARMS; k++)
	              delta[k] = (k==j) ? -2*parms[k]*DELTAP : 0.0;

	            d = dNudge(delta); // resid at pminus
	            if (d==BIGVAL)
	            {
	                System.out.println("Bad dBuildJacobian() exit 3"); 
	                return false;  
	            }
	            
	            for (int i=0; i<NPTS; i++){
	            	for (int k = 0; k < NRESP; k++){
	            		jac[i][j][k] -= dGetResid(i,k);	// fetches resid[]
	            	}
	            }
	            for (int i=0; i<NPTS; i++){
	            	for (int k = 0; k < NRESP; k++){
	            		jac[i][j][k] *= FACTOR;
	            	}
	            	
	            }
	            for (int k=0; k<NPARMS; k++)
	              delta[k] = (k==j) ? parms[k]*DELTAP : 0.0;

	            d = dNudge(delta);  
	            if (d==BIGVAL)
	            {
	                System.out.println("Bad dBuildJacobian() exit 4"); 
	                return false;  
	            }
	        }
	        return true; 
	}
	/**
	 * Jacobian with forward finite difference, to limit the number of extra "getModelValues()" calls,
	 * 1 instead of 2 for the two-sided finite difference method
	 */
	public boolean bBuildJacobian_forward() throws Exception {
		// Allows LM to compute a new Jacobian.
	    // Uses current parms[] and one-sided forward finite difference.
		// df/dx = ( f(x+h) - f(x) ) / h
	    // If current parms[] is bad, returns false.  
	    
	        double delta[] = new double[NPARMS];
	        double FACTOR = 1 / DELTAP; 
	        double d=0; 

	        for (int j=0; j<NPARMS; j++)
	        {	            
	        	for (int i=0; i<NPTS; i++){
	        		for (int k = 0; k < NRESP; k++){
	        			  jac[i][j][k] = -dGetResid(i,k); //resid central point
	        		}
	        	}   
     	
	        	for (int k=0; k<NPARMS; k++)
	              delta[k] = (k==j) ? parms[k]*DELTAP : 0.0;

	            d = dNudge(delta); // resid at pplus
	            if (d==BIGVAL)
	            {
	                System.out.println("Bad dBuildJacobian() exit 2"); 
	                return false;  
	            }
	            for (int i=0; i<NPTS; i++){
	            	for (int k = 0; k < NRESP; k++){
	            		jac[i][j][k] += dGetResid(i,k);	
	            	}
	              
	            }
	            for (int i=0; i<NPTS; i++){
	            	for (int k = 0; k < NRESP; k++){
	            		jac[i][j][k] *= FACTOR;	
	            	}
	            }
	            for (int k=0; k<NPARMS; k++)
		              delta[k] = (k==j) ? parms[k]*DELTAP : 0.0;

		        d = dNudge(delta);  
		        if (d==BIGVAL)
		        	{
		            System.out.println("Bad dBuildJacobian() exit 4"); 
		            return false;  
		        	}
	        }
	        return true; 
	}
	public double dComputeResid() throws Exception {
		// Evaluates residual matrix for parms[].
		// Returns sum-of-squares.
		/**
		 * TODO deal with CKSolnList flag, employed in .getModelValues()
		 */
		Function f = new Function (optimization.getModelValues(optimization.buildFullParamVector(parms),true), optimization.getExp());
		resid = f.getResid();
		return f.getSSQ();
	}

	public double dGetJac(int i, int j, int k) {
		 // Allows LM to get one element of the Jacobian matrix. 
	    
	        return jac[i][j][k]; 
	    }
	public double[][][] dGetFullJac(){
		return jac;
	}
	public double dGetResid(int i, int j) {
		 // Allows LM to get one element of the resid[] vector. 
	    
	        return resid[i][j];
	    }

	public double dNudge(double[] dp) throws Exception {
		// Allows LM to modify parms[] and reevaluate.
	    // Returns sum-of-squares for nudged params.
	    // This is the only place that parms[] are modified.
	    // If NADJ<NPARMS, this is the place for your LUT.
	    
	        for (int j=0; j<NPARMS; j++)
	          parms[j] += dp[j]; 
	        return dComputeResid(); 
	}
	/**
	 * return_optimized_parameters()
	 * @return
	 * @author nmvdewie
	 */
	public double [] return_optimized_parameters(){
		return parms;
	}
}
