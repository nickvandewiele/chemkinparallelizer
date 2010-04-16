package parameter_estimation;



//implementation from: http://www.ibm.com/developerworks/library/j-thread.html

public class Semaphore {
   private volatile int count;
   public Semaphore(int n) {
      this.count = n;
   }

   public synchronized void acquire() {
      while(count == 0) {
         try {
            wait();
         } catch (InterruptedException e) {
            //keep trying
         }
      }
      count--;
   }
	
   public synchronized void release() {
      count++;
      notify(); //alert a thread that's blocking on this semaphore
   }
}