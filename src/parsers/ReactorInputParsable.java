package parsers;

import java.io.File;
import java.util.List;

import readers.ReactorInput;

/**
 * Interface for all classes that parse a file, eg an excel file, with operating conditions
 * required to create separate reactor input files
 * @author nmvdewie
 *
 */
public interface ReactorInputParsable {
	/**
	 * The global method 
	 * @param excelfile 
	 * @return
	 */
	public List<ReactorInput> parse();
	
	/**
	 * The reader method that reads in the reactor input database containing the operating conditions
	 * required to create a reactor input file
	 * @param excelfile 
	 */
	public void read();
	
	/**
	 * The writer method that returns a list of filenames corresponding to the created reactor input filenames.
	 */
	public void write();
}
