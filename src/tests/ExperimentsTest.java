package tests;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import datamodel.Experiments;


public class ExperimentsTest {
	Experiments experiments;
	@Before
	public void setUp() throws Exception {
		experiments = new Experiments();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadResponseVariables() {
		String path = System.getProperty("user.dir")+"/data"+"/test - H2O2-fictional/exp.csv";
		try {
			BufferedReader in = new BufferedReader(new FileReader(path));
			LinkedList<String> responseVariables = experiments.getResponseVariables().readEffluentResponses(in);
			
			Assert.assertEquals("H2",responseVariables.get(0));
			Assert.assertEquals("O2",responseVariables.get(1));
			Assert.assertEquals("H2O",responseVariables.get(2));
			Assert.assertEquals("H2O2",responseVariables.get(3));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testReadExperimentalEffluents(){
		String path = System.getProperty("user.dir")+"/data/test - H2O2-fictional/"+"exp.csv";
		try {
			BufferedReader in = new BufferedReader(new FileReader(path));
			int noRegularExperiments = 8;
			try {
				LinkedList<Map<String, Double>> experimentalEffluents = experiments.readExperimentalEffluents(in,
						noRegularExperiments);
				Assert.assertNotNull(experimentalEffluents);
				Assert.assertEquals(experimentalEffluents.size(), 8);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
