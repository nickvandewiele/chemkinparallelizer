package levenberg.multi;

import java.io.FileWriter;
import java.io.PrintWriter;

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
class LMmultiD    
{
    private final int    LMITER     =  100;     // max number of L-M iterations
    private final double LMBOOST    =  2.0;     // damping increase per failed step
    private final double LMSHRINK   = 0.10;     // damping decrease per successful step
    private final double LAMBDAZERO = 0.001;    // initial damping
    private final double LAMBDAMAX  =  1E3;     // max damping
    private final double LMTOL      = 1E-12;    // exit tolerance
    private final double BIGVAL = 9.876543E+210; 
 
    private double sos, sosprev, lambda;

    private LMhostmultiD myH = null;    // overwritten by constructor
    private int nadj = 0;         // overwritten by constructor
    private int npts = 0;         // overwritten by constructor
    private int nresp = 0;         // overwritten by constructor

    private double[] delta;       // local, step size vector for parameters
    private double[] beta;        // local, JT*resid vector
    private double[][] alpha;     // local, JT.J matrix
    private double[][] amatrix;   // local, alpha' matrix = JT.J + lambda*I

    public LMmultiD(LMhostmultiD gH, int gnadj, int gnpts, int nresp) throws Exception
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
            done = bLMiter();
            niter++;
            System.out.println("niter: "+niter);
            out.println("niter: "+niter);
        } 
        while (!done && (niter<LMITER));
        out.close();
    }

    private boolean bLMiter( ) throws Exception
    // Each call performs one LM iteration. 
    // Returns true if done with iterations; false=wants more. 
    // Global nadj, npts; needs nadj, myH to be preset. 
    // Ref: M.Lampton, Computers in Physics v.11 pp.110-115 1997.
    {
        PrintWriter out = new PrintWriter(new FileWriter("LM.txt"));
        PrintWriter out_SSQ = new PrintWriter (new FileWriter("SSQ_LM.txt"));
    	sos = myH.dComputeResid();
    	
        if (sos==BIGVAL)
        {
           System.out.println("bLMiter finds faulty initial dComputeResid()");
           return false; 
        }
        sosprev = sos;
        
        System.out.println("sosprev: "+sosprev);
        out.println("sosprev: "+sosprev);
        //out.println("bLMiter..sos= "+sos);
        
        if (!myH.bBuildJacobian_forward())
        {
        	
        	System.out.println("bLMiter finds bBuildJacobian()=false"); 
            return false;
        }
        
        System.out.println("jacobian[i][j][k]: ");
    	out.println("jacobian[i][j][k]: ");
    	print3DMatrix(myH.dGetFullJac(),out);
    	
        for (int k=0; k<nadj; k++)      // get downhill gradient beta
        {
            beta[k] = 0.0;
            for (int i=0; i<npts; i++){
            	for (int j = 0; j < nresp; j++){
            		beta[k] -= myH.dGetResid(i,j)*myH.dGetJac(i,k,j);
            	}
            }
        }
        
        System.out.println("beta[i]: ");
        out.println("beta[i]: ");
        printArray(beta, out);
        
        for (int k=0; k<nadj; k++)      // get curvature matrix alpha
          for (int j=0; j<nadj; j++)
          {
              alpha[j][k] = 0.0;
              for (int i=0; i<npts; i++){
            	  for (int l = 0; l < nresp; l++){
            		  alpha[j][k] += myH.dGetJac(i,j,l)*myH.dGetJac(i,k,l);	  
            	  }
              }
          }
        System.out.println("alpha[i][j]: ");
        out.println("alpha[i][j]: ");
        printMatrix(alpha,out);
        
        double rrise = 0; 
        do  /// damping loop searches for one downhill step
        {
            // System.out.println("  lambda = "+lambda); 
            for (int k=0; k<nadj; k++)       // copy and damp it
              for (int j=0; j<nadj; j++)
                amatrix[j][k] = alpha[j][k] + ((j==k) ? lambda : 0.0);
            
            System.out.println("amatrix[i][j]: ");
            out.println("amatrix[i][j]: ");
            printMatrix(amatrix,out);
            
            gaussj(amatrix, nadj);           // invert
            
            System.out.println("amatrix[i][j] inverted: ");
            out.println("amatrix[i][j] inverted: ");
            printMatrix(amatrix,out);
            
            for (int k=0; k<nadj; k++)       // compute delta[]
            {
                delta[k] = 0.0; 
                for (int j=0; j<nadj; j++)
                  delta[k] += amatrix[j][k]*beta[j];
            }
            
            System.out.println("delta[k]: ");
            out.println("delta[k]: ");
            printArray(delta,out);
            
            sos = myH.dNudge(delta);         // try it out.
            out_SSQ.println("SSQ: "+sos);
            
            if (sos==BIGVAL)
            {
                System.out.println("LMinner failed SOS step"); 
                return false;            
            }
            
            rrise = (sos-sosprev)/(1+sos);
            System.out.println("rrise: "+rrise);
            out.println("rrise: "+rrise);
            
            if (rrise <= 0.0)                // good step!
            {
               out.println("lambda: "+lambda);
               System.out.println("lambda: "+lambda);
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
            System.out.println("lambda: "+lambda);
            
        } while (lambda<LAMBDAMAX);
        boolean done = (rrise>-LMTOL) || (lambda>LAMBDAMAX); 
        
        out.close();
        out_SSQ.close();
        return done; 
    }

    public double gaussj( double[][] a, int N )
    // Inverts the double array a[N][N] by Gauss-Jordan method
    // M.Lampton UCB SSL (c)2003, 2005
    {
        double det = 1.0, big, save;
        int i,j,k,L;
        int[] ik = new int[100];
        int[] jk = new int[100];
        for (k=0; k<N; k++)
        {
            big = 0.0;
            for (i=k; i<N; i++)
              for (j=k; j<N; j++)          // find biggest element
                if (Math.abs(big) <= Math.abs(a[i][j]))
                {
                    big = a[i][j];
                    ik[k] = i;
                    jk[k] = j;
                }
            if (big == 0.0) return 0.0;
            i = ik[k];
            if (i>k)
              for (j=0; j<N; j++)          // exchange rows
              {
                  save = a[k][j];
                  a[k][j] = a[i][j];
                  a[i][j] = -save;
              }
            j = jk[k];
            if (j>k)
              for (i=0; i<N; i++)
              {
                  save = a[i][k];
                  a[i][k] = a[i][j];
                  a[i][j] = -save;
              }
            for (i=0; i<N; i++)            // build the inverse
              if (i != k)
                a[i][k] = -a[i][k]/big;
            for (i=0; i<N; i++)
              for (j=0; j<N; j++)
                if ((i != k) && (j != k))
                  a[i][j] += a[i][k]*a[k][j];
            for (j=0; j<N; j++)
              if (j != k)
                a[k][j] /= big;
            a[k][k] = 1.0/big;
            det *= big;                    // bomb point
        }                                  // end k loop
        for (L=0; L<N; L++)
        {
            k = N-L-1;
            j = ik[k];
            if (j>k)
              for (i=0; i<N; i++)
              {
                  save = a[i][k];
                  a[i][k] = -a[i][j];
                  a[i][j] = save;
              }
            i = jk[k];
            if (i>k)
              for (j=0; j<N; j++)
              {
                  save = a[k][j];
                  a[k][j] = -a[i][j];
                  a[i][j] = save;
              }
        }
        return det;
    }
    
    public void printArray(double [] d, PrintWriter out){
    	for (int i = 0; i < d.length; i++) {
			out.print(d[i]+" ");
			System.out.print(d[i]+" ");
		}
    	out.println();
    	System.out.println();
    }
    public void printMatrix(double [][] d,PrintWriter out){
    	for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				out.print(d[i][j]+" ");
				System.out.print(d[i][j]+" ");			
			}
			out.println();
	    	System.out.println();
		}
    	out.println();
    	System.out.println();
    }
    public void print3DMatrix(double [][][] d,PrintWriter out){
    	for (int i = 0; i < d.length; i++) {
			for (int j = 0; j < d[0].length; j++) {
				for (int k = 0; k < d[0][0].length; k++) {
					out.print(d[i][j][k]+" ");
					System.out.print(d[i][j][k]+" ");			
				}
				out.println();
		    	System.out.println();
			}
			out.println();
	    	System.out.println();
		}
    	out.println();
    	System.out.println();
    }
    
} //-----------end of class LM--------------------
