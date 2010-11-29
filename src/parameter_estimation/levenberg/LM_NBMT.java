package parameter_estimation.levenberg;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

import parameter_estimation.Algebra;
import parameter_estimation.ParameterEstimationDriver;
import parameter_estimation.Printer;

/**
  *  class LM   Levenberg Marquardt w/ Lampton improvements
  *  M.Lampton, 1997 Computers In Physics v.11 #10 110-115.
  *
  *  Constructor is used to set up all parms including host for callback.
  *  bLMiter() performs one iteration.
  *  Host arrays parms[], resid[], jac[][] are unknown here.
  *  Callback method uses CallerID to access five host methods:
  *    double dComputeResid();    Returns sos, or BIGVAL if parms failed.
  *    double dNudge(dp);         Moves parms, builds resid[], returns sos.
  *    boolean bBuildJacobian();  Builds Jacobian, returns false if parms NG.
  *    double dGetJac(i,j);       Fetches one value of host Jacobian.
  *    double dGetResid(i);       Fetches one value of host residual.
  *  Exit leaves host with optimized parms[]. 
  *
  *  @author: M.Lampton UCB SSL (c) 2005
  */
public class LM_NBMT    
{
	public static Logger logger = Logger.getLogger(ParameterEstimationDriver.logger.getName());
    private final int    LMITER     =  100;     // max number of L-M iterations
    private final double LMBOOST    =  2.0;     // damping increase per failed step
    private final double LMSHRINK   = 0.10;     // damping decrease per successful step
    private final double LAMBDAZERO = 0.001;    // initial damping
    private final double LAMBDAMAX  =  1E3;     // max damping
    private final double LMTOL      = 1E-12;    // exit tolerance
    private final double BIGVAL = 9.876543E+210; 
 
    private double sos, sosprev, lambda;

    private NBMTHost myH = null;    // overwritten by constructor
    private int nadj = 0;         // overwritten by constructor
    private int npts = 0;         // overwritten by constructor
    private int nresp = 0;         // overwritten by constructor

    private double[] delta;       // local, step size vector for parameters
    private double[] beta;        // local, JT*resid vector
    private double[][] alpha;     // local, JT.J matrix
    private double[][] amatrix;   // local, alpha' matrix = JT.J + lambda*I

    public LM_NBMT(NBMTHost gH, int gnadj, int gnpts, int nresp) throws IOException, InterruptedException
    // Constructor sets up fields and drives iterations. 
    {
        myH = gH;
        nadj = gnadj;
        npts = gnpts;  
        this.nresp = nresp;
        
        delta = new double[nadj];
        beta = new double[nadj];
        alpha = new double[nadj][nadj]; 
        amatrix = new double[nadj][nadj];
        lambda = LAMBDAZERO; 
        int niter = 0; 
        boolean done = false;
        PrintWriter out = new PrintWriter(new FileWriter("LM.txt"));
        do
        {
            done = bLMiter(out);
            niter++;
            
            out.println("New parameters: ");
            Printer.printArray(myH.getParms(),out);
            logger.info("niter: "+niter);
            out.println("niter: "+niter);
            
        } 
        while (!done && (niter<LMITER));
        out.close();

    }

    private boolean bLMiter(PrintWriter out ) throws IOException, InterruptedException
    // Each call performs one LM iteration. 
    // Returns true if done with iterations; false=wants more. 
    // Global nadj, npts; needs nadj, myH to be preset. 
    // Ref: M.Lampton, Computers in Physics v.11 pp.110-115 1997.
    {
        //PrintWriter out = new PrintWriter(new FileWriter("LM.txt"));
        PrintWriter out_SSQ = new PrintWriter (new FileWriter("SSQ_LM.txt"));
    	sos = myH.dComputeResid();
    	
        if (sos==BIGVAL)
        {
           logger.debug("bLMiter finds faulty initial dComputeResid()");
           return false; 
        }
        sosprev = sos;
        
        logger.info("sosprev: "+sosprev);
        out.println("sosprev: "+sosprev);
        //out.println("bLMiter..sos= "+sos);
        
        //if (!myH.bBuildJacobian_forward())
        if (!myH.bBuildJacobian())
        {
        	
        	logger.debug("bLMiter finds bBuildJacobian()=false"); 
            return false;
        }
        
        logger.info("jacobian[i][j]: ");
    	out.println("jacobian[i][j]: ");
    	Printer.printMatrix(myH.dGetFullJac(),out);
    	
        for (int k=0; k<nadj; k++)      // get downhill gradient beta
        {
            beta[k] = 0.0;
            for (int i=0; i<npts*nresp; i++)
            		beta[k] -= myH.dGetResid(i)*myH.dGetJac(i,k);
            
        }
        
        logger.info("beta[i]: ");
        out.println("beta[i]: ");
        Printer.printArray(beta, out);
        
        for (int k=0; k<nadj; k++)      // get curvature matrix alpha
          for (int j=0; j<nadj; j++)
          {
              alpha[j][k] = 0.0;
              for (int i=0; i<npts*nresp; i++)
            		  alpha[j][k] += myH.dGetJac(i,j)*myH.dGetJac(i,k);	  	  
          }
        
        logger.info("alpha[i][j]: ");
        out.println("alpha[i][j]: ");
        Printer.printMatrix(alpha,out);
        
        double rrise = 0; 
        do  /// damping loop searches for one downhill step
        {
            // System.out.println("  lambda = "+lambda); 
            for (int k=0; k<nadj; k++)       // copy and damp it
              for (int j=0; j<nadj; j++)
                amatrix[j][k] = alpha[j][k] + ((j==k) ? lambda : 0.0);
            
            logger.info("amatrix[i][j]: ");
            out.println("amatrix[i][j]: ");
            Printer.printMatrix(amatrix,out);
            
            Algebra.gaussj(amatrix, nadj);           // invert
            
            logger.info("amatrix[i][j] inverted: ");
            out.println("amatrix[i][j] inverted: ");
            Printer.printMatrix(amatrix,out);
            
            for (int k=0; k<nadj; k++)       // compute delta[]
            {
                delta[k] = 0.0; 
                for (int j=0; j<nadj; j++)
                  delta[k] += amatrix[j][k]*beta[j];
            }
            
            logger.info("delta[k]: ");
            out.println("delta[k]: ");
            Printer.printArray(delta,out);
            
            sos = myH.dNudge(delta);         // try it out.
            out_SSQ.println("SSQ: "+sos);
            
            if (sos==BIGVAL)
            {
                logger.info("LMinner failed SOS step"); 
                return false;            
            }
            
            rrise = (sos-sosprev)/(1+sos);
            logger.info("rrise: "+rrise);
            out.println("rrise: "+rrise);
            
            if (rrise <= 0.0)                // good step!
            {
               out.println("lambda: "+lambda);
               logger.info("lambda: "+lambda);
               lambda *= LMSHRINK;           // shrink lambda
               break;                        // leave lmInner.
            }
            for (int q=0; q<nadj; q++)       // reverse course!
               delta[q] *= -1.0;
            myH.dNudge(delta);               // sosprev should still be OK
            if (rrise < LMTOL)               // finished but keep prev parms
              break;                         // leave inner loop
            lambda *= LMBOOST;               // else try more damping.
            
            out.println("lambda: "+lambda);
            logger.info("lambda: "+lambda);
            
        } while (lambda<LAMBDAMAX);
        boolean done = (rrise>-LMTOL) || (lambda>LAMBDAMAX); 
        
        //out.close();
        out_SSQ.close();
        return done; 
    }
    
    
} //-----------end of class LM--------------------
