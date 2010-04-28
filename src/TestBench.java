import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TestBench {

	/**
	 * @param args
	 * @throws Exception 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception{
		Set<String> s  = new HashSet<String>();
		s.add("Hond");
		s.add("Kat");
		s.add("Giraf");
		for(Iterator<String> it = s.iterator(); it.hasNext();){
			System.out.println(it.next());
		}
	}
}