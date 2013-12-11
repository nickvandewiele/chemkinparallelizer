package tests;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.Tools;
import applications.ParameterEstimationDriver;

public class ParameterEstimationTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testMain0() {
		/**
		 * test parity mode
		 */
		String pathWorkingDir = System.getProperty("user.dir")+"/data"+"/test - H2O2-fictional/";
		String args[] = {pathWorkingDir+"INPUT-mode0.txt"};
		try {
			ParameterEstimationDriver.main(args);
			//checkOutputFolder(pathWorkingDir);
			//checkParity(pathWorkingDir);
			//deleteOutput(pathWorkingDir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private void checkStatistics(String pathWorkingDir) {
		//check if parity plot file exists:
		File parity = new File(pathWorkingDir+"/output/Statistics.txt");
		Assert.assertNotNull(parity);	

		
	}

	public void checkOutputFolder(String pathWorkingDir){
		//check if output folder exists:
		File outputFolder = new File(pathWorkingDir+"/output/");
		Assert.assertNotNull(outputFolder);
		
	}
	public void checkParity(String pathWorkingDir){
		//check if parity plot file exists:
		File parity = new File(pathWorkingDir+"/output/SpeciesParity.csv");
		Assert.assertNotNull(parity);	
	}
	public void deleteOutput(String pathWorkingDir){
		File outputFolder = new File(pathWorkingDir+"/output/");
		try {
			FileUtils.deleteDirectory(outputFolder);
		} catch (IOException e) {}
	}
}
