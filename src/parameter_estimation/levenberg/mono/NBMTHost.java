package parameter_estimation.levenberg.mono;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import parameter_estimation.Function;
import parameter_estimation.Optimization;
import parameter_estimation.ParameterEstimationDriver;

public class NBMTHost implements NBMThostI {
	static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
	//--------constants---------------
    protected final double DELTAP = 1e-4;//1e-6
    protected final double BIGVAL = 9.876543E+210; 
    protected int NPTS, NPARMS, NRESP, NEXPL; //modified by constructor

    //--------fields------------------
    
    //private double resid[][]; //residual matrix Y - Y^ with rows as experiments
    private double resid[]; //residual matrix Y - Y^ with rows as experiments
    private double jac[][];  //2D jacobian
    private double parms[];  // starting point
    
    private Optimization optimization;
    private Function function;
    private LM_NBMT myLM;
    
    private boolean additive_finite_difference = false; //additive implies that step in parameter direction will be regardless of parameter value
    
    public NBMTHost(Optimization o) throws IOException, InterruptedException
    {
    	this.optimization = o;
    	this.parms = optimization.retrieve_fitted_parameters();
    	
    	NPARMS = parms.length;
    	NPTS = (optimization.getExp()).size();
    	NRESP = (optimization.getExp().get(0)).size();
    	
    	//resid = new double[NPTS][NRESP];
    	resid = new double[NPTS*NRESP];
    	jac = new double[NPTS*NRESP][NPARMS];
    	
    	for (int i=0; i<NPARMS; i++)
          logger.info("Start parm["+i+"] = "+parms[i]); 

        myLM = new LM_NBMT(this, NPARMS, NPTS, NRESP); // run the minimizer
        
        for (int i=0; i<NPARMS; i++)
        	logger.info("End parm["+i+"]   = "+parms[i]); 
    }
    
    /**
     * constructor for statistics mode in which only jacobian needs to be calculated
     * 
     * @param o
     * @param stat is there to distinguish with other constructor
     * @throws Exception
     */
    
    public NBMTHost(Optimization o, boolean stat) throws IOException, InterruptedException
    {
    	this.optimization = o;
    	this.parms = optimization.retrieve_fitted_parameters();
    	
    	NPARMS = parms.length;
    	NPTS = (optimization.getExp()).size();
    	NRESP = (optimization.getExp().get(0)).size();
    	
    	//resid = new double[NPTS][NRESP];
    	resid = new double[NPTS*NRESP];
    	jac = new double[NPTS*NRESP][NPARMS];

       
    }
	public boolean bBuildJacobian() throws IOException, InterruptedException {
		// Allows LM to compute a new Jacobian.
	    // Uses current parms[] and two-sided finite difference.
	    // If current parms[] is bad, returns false.  
		// df/dx = ( f(x+h) - f(x-h) ) / 2h
		// 1st step: reset parameters to x+h, calculate f(x+h), df/dx = f(x+h)
		// 2nd step: reset parameters to x-h, calculate f(x-h), df/dx -= f(x-h)
		// 3rd step: df/dx *= 1/2h
		// 4th step: reset parameters to x (by adding h to x)
	        double delta[] = new double[NPARMS];
	        double FACTOR = 0.0;//initialized at zero for security reasons
	          
	        double d=0; 

	        for (int j=0; j<NPARMS; j++)
	        {
	            for (int k=0; k<NPARMS; k++){
	            	if(additive_finite_difference){
	            		delta[k] = (k==j) ? DELTAP : 0.0;
	            		if (k==j) FACTOR = 0.5 / DELTAP;
	            	}
	            	else{
	            		delta[k] = (k==j) ? parms[k] * DELTAP : 0.0;
	            		if (k==j) FACTOR = 0.5 / (DELTAP * parms[k]);
	            	}
	            }
	              

	            d = dNudge(delta); // resid at pplus
	            if (d==BIGVAL)
	            {
	                logger.debug("Bad dBuildJacobian() exit 2"); 
	                return false;  
	            }
	            
	            for (int i=0; i<NPTS*NRESP; i++)
	            		jac[i][j] = dGetResid(i);//fill up 2D jacobian with residuals
	            	
	            

	            for (int k=0; k<NPARMS; k++){
	            	if(additive_finite_difference){
	            		delta[k] = (k==j) ? -2*DELTAP : 0.0;
	            	}
	            	else{
	            		delta[k] = (k==j) ? -2*parms[k] * DELTAP : 0.0;	
	            	}
	            }

	            d = dNudge(delta); // resid at pminus
	            if (d==BIGVAL)
	            {
	                logger.debug("Bad dBuildJacobian() exit 3"); 
	                return false;  
	            }
	            
	            for (int i=0; i<NPTS*NRESP; i++)
            		jac[i][j] -= dGetResid(i);//fill up 2D jacobian with residuals
	              
	            for (int i=0; i<NPTS*NRESP; i++)
	            		jac[i][j] *= FACTOR;
	           	           
	            for (int k=0; k<NPARMS; k++){
	            	if(additive_finite_difference){
	            		delta[k] = (k==j) ? DELTAP : 0.0;
	            	}
	            	else{
	            		delta[k] = (k==j) ? parms[k] * DELTAP : 0.0;	
	            	}         
	            }

	            d = dNudge(delta);  
	            if (d==BIGVAL)
	            {
	                logger.debug("Bad dBuildJacobian() exit 4"); 
	                return false;  
	            }
	        }
	        return true; 
	}
	/**
	 * Jacobian with forward finite difference, to limit the number of extra "getModelValues()" calls,
	 * 1 instead of 2 for the two-sided finite difference method
	 */
	public boolean bBuildJacobian_forward() throws IOException, InterruptedException {
		// Allows LM to compute a new Jacobian.
	    // Uses current parms[] and one-sided forward finite difference.
		// df/dx = ( f(x+h) - f(x) ) / h
		// 1st step: df/dx = -f(x)
		// 2nd step: calculate f(x+h)
		// 3rd step: df/dx += f(x+h)
		// 4th step: df/dx *= 1/h
		// 5th step: reset parameters to central point (substracting h from x)
	    // If current parms[] is bad, returns false.  
	    
	        double delta[] = new double[NPARMS];
	        double FACTOR = 0.0;//initialized at zero for security reasons
	       	        double d=0; 

	        for (int j=0; j<NPARMS; j++)
	        {	            
	        	for (int k = 0; k < NPARMS; k++){
	        		delta[k] = 0;
	        	}
	        	dNudge(delta);
	        	
	            for (int i=0; i<NPTS*NRESP; i++)
	            		jac[i][j] = -dGetResid(i);//fill up 2D jacobian with residuals	
	             
	        	for (int k=0; k<NPARMS; k++){
	        		if(additive_finite_difference){
	            		delta[k] = (k==j) ? DELTAP : 0.0;
	            		FACTOR = 1 / DELTAP;
	            	}
	            	else{
	            		delta[k] = (k==j) ? parms[k] * DELTAP : 0.0;	
	            		FACTOR = 1 / (DELTAP * parms[k]);
	            	}
	            }

	            d = dNudge(delta); // resid at pplus
	            if (d==BIGVAL)
	            {
	                logger.debug("Bad dBuildJacobian() exit 2"); 
	                return false;  
	            }
	            
	            for (int i=0; i<NPTS*NRESP; i++)
            		jac[i][j] += dGetResid(i);//pplus	
             
	            
	            for (int i=0; i<NPTS*NRESP; i++)
            		jac[i][j] *= FACTOR;
	            
	            for (int k=0; k<NPARMS; k++){//reset parameters to central point
	            	if(additive_finite_difference){
	            		delta[k] = (k==j) ? -DELTAP : 0.0;
	            	}
	            	else{
	            		delta[k] = (k==j) ? -parms[k] * DELTAP : 0.0;	
	            	}
	            }
	            	
		        d = dNudge(delta);  
		        if (d==BIGVAL)
		        	{
		            logger.debug("Bad dBuildJacobian() exit 4"); 
		            return false;  
		        	}
	        }
	        return true; 
	}
	public double dComputeResid() throws IOException, InterruptedException {
		// Evaluates residual matrix for parms[].
		// Returns sum-of-squares.
		/**
		 * TODO deal with CKSolnList flag, employed in .getModelValues()
		 */
		function = new Function (optimization.getModelValues(optimization.buildFullParamVector(parms),true), optimization.getExp());
		resid = function.getResid();
		return function.getSRES();
	}

	public double dGetJac(int i, int j) {
		 // Allows LM to get one element of the Jacobian matrix. 
	    
	        return jac[i][j]; 
	    }
	public double[][] dGetFullJac(){
		return jac;
	}
	//public double dGetResid(int i, int j) {
	public double dGetResid(int i) {
		 // Allows LM to get one element of the resid[] vector. 
	    
	        return resid[i];
	    }

	public double dNudge(double[] dp) throws IOException, InterruptedException {
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
	public double [] getParms(){
		return parms;
	}
	public Function getFunction(){
		return function;
	}

	public LM_NBMT getMyLM() {
		return myLM;
	}

	public int getNPTS() {
		return NPTS;
	}

	public int getNPARMS() {
		return NPARMS;
	}

	public int getNRESP() {
		return NRESP;
	}
   public void printArray(double [] d, PrintWriter out){
	    for (int i = 0; i < d.length; i++) {
			out.print(d[i]+" ");
			logger.info(d[i]+" ");
		}
	    out.println();
	    //System.out.println();
   }
}
