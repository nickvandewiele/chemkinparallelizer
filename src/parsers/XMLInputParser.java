package parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import parameter_estimation.Licenses;
import parameter_estimation.OptimizedReaction;
import readers.ExperimentalDatabaseInput;
import readers.ReactorSetupInput;

public class XMLInputParser {
	
	static final String INPUT = "input";
	static final String WORKING_DIR = "working_dir";
	static final String CHEMKIN_DIR = "chemkin_dir";
	static final String NO_LICENSES = "no_licenses";
	static final String CHEMISTRY_INPUT = "chemistry_input";
	static final String TOTAL_NUMBER_OF_EXPERIMENTS = "total_number_of_experiments";
	static final String EXPERIMENTAL_DATABASES = "experimental_databases";
	static final String EXPERIMENTAL_DATABASE = "experimental_database";
	static final String REACTOR_SETUPS = "reactor_setups";
	static final String REACTOR_SETUP = "reactor_setup";
	static final String TYPE = "type";
	static final String MODEL = "model";
	static final String LOCATION = "location";
	static final String MODE = "mode";
	static final String FITTING = "fitting";
	static final String ROSENBROCK = "rosenbrock";
	static final String NUMBEROFEVALUATIONS = "number_of_evaluations";
	static final String REACTIONS = "optimized_reactions";
	static final String REACTION = "reaction";
	static final String A = "A";
	static final String N = "n";
	static final String EA = "Ea";

	@SuppressWarnings({ "unchecked", "null" })
	public List<ConfigurationInput> readConfig(String configFile) {
		List<ConfigurationInput> configurationInputs = new ArrayList<ConfigurationInput>();
		try {
			// First create a new XMLInputFactory
			XMLInputFactory inputFactory = XMLInputFactory.newInstance();
			// Setup a new eventReader
			InputStream in = new FileInputStream(configFile);
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
			// Read the XML document
			ConfigurationInput configurationInput = null;

			List<ExperimentalDatabaseInput> list_expdb = null;
			List<ReactorSetupInput> list_reactor_setup = null;
			List<OptimizedReaction> reactions = null;
			while (eventReader.hasNext()) {
				XMLEvent event = eventReader.nextEvent();

				if (event.isStartElement()) {
					StartElement startElement = event.asStartElement();
					// If we have a ConfigurationInput element we create a new ConfigurationInput
					if (startElement.getName().getLocalPart() == (INPUT)) {
						configurationInput = new ConfigurationInput();
					}
					if (event.asStartElement().getName().getLocalPart()
							.equals(CHEMKIN_DIR)) {
						event = eventReader.nextEvent();
						configurationInput.paths.setChemkinDir(event.asCharacters().getData());
						continue;
					}

					if (event.asStartElement().getName().getLocalPart()
							.equals(NO_LICENSES)) {
						event = eventReader.nextEvent();
						configurationInput.licenses = new Licenses(Integer.parseInt(event.asCharacters().getData()));
						continue;
					}

					if (event.asStartElement().getName().getLocalPart()
							.equals(CHEMISTRY_INPUT)) {
						event = eventReader.nextEvent();
						configurationInput.chemistry.setChemistryInput(event.asCharacters().getData());
						continue;
					}

					if (event.asStartElement().getName().getLocalPart()
							.equals(TOTAL_NUMBER_OF_EXPERIMENTS)) {
						event = eventReader.nextEvent();
						configurationInput.experiments.total_no_experiments = Integer.parseInt(event.asCharacters().getData());
						continue;
					}

					if (event.asStartElement().getName().getLocalPart()
							.equals(EXPERIMENTAL_DATABASES)) {
						event = eventReader.nextEvent();
						list_expdb = new ArrayList<ExperimentalDatabaseInput>();
						continue;
					}
					
					if (event.asStartElement().getName().getLocalPart()
							.equals(EXPERIMENTAL_DATABASE)) {
						ExperimentalDatabaseInput exp_input = new ExperimentalDatabaseInput();
						// attribute to our object
						Iterator<Attribute> attributes = event.asStartElement()
								.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(TYPE)) {
								exp_input.setType(attribute.getValue());
							}
						}
						event = eventReader.nextEvent();
						exp_input.setLocation(new File(event.asCharacters().getData()));
						
						list_expdb.add(exp_input);
						continue;
					}
					if (event.asStartElement().getName().getLocalPart()
							.equals(REACTOR_SETUPS)) {
						event = eventReader.nextEvent();
						list_reactor_setup = new ArrayList<ReactorSetupInput>();
						continue;
					}
					
					if (event.asStartElement().getName().getLocalPart()
							.equals(REACTOR_SETUP)) {
						ReactorSetupInput reactor_input = new ReactorSetupInput();
						// attribute to our object
						Iterator<Attribute> attributes = event.asStartElement()
								.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(TYPE)) {
								reactor_input.setType(attribute.getValue());
							}
							if (attribute.getName().toString().equals(MODEL)) {
								reactor_input.setModel(attribute.getValue());
							}
						}
						event = eventReader.nextEvent();
						reactor_input.setLocation(event.asCharacters().getData());
						
						list_reactor_setup.add(reactor_input);
						continue;
					}
					if (event.asStartElement().getName().getLocalPart()
							.equals(MODE)) {
						event = eventReader.nextEvent();
						configurationInput.setMODE(Integer.parseInt(event.asCharacters().getData()));
						continue;
					}
					
					if (event.asStartElement().getName().getLocalPart()
							.equals(FITTING)) {
						event = eventReader.nextEvent();
						//
						configurationInput.fitting.method = event.asCharacters().getData();
						continue;
					}
					
					if (event.asStartElement().getName().getLocalPart()
							.equals(ROSENBROCK)) {
						// attribute to our object
						Iterator<Attribute> attributes = event.asStartElement()
								.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(NUMBEROFEVALUATIONS)) {
								configurationInput.fitting.setMaxNoRosenbrockEvaluations(Integer.parseInt(attribute.getValue()));
							}
						}
						event = eventReader.nextEvent();

						continue;
					}
					if (event.asStartElement().getName().getLocalPart()
							.equals(REACTIONS)) {
						event = eventReader.nextEvent();
						reactions = new ArrayList<OptimizedReaction>();
						continue;
					}
					if (event.asStartElement().getName().getLocalPart()
							.equals(REACTION)) {
						OptimizedReaction reaction = new OptimizedReaction(); 
						// attribute to our object
						Iterator<Attribute> attributes = event.asStartElement()
								.getAttributes();
						while (attributes.hasNext()) {
							Attribute attribute = attributes.next();
							if (attribute.getName().toString().equals(A)) {
								reaction.A = (Integer.parseInt(attribute.getValue()));
							}
							else if (attribute.getName().toString().equals(N)) {
								reaction.N = (Integer.parseInt(attribute.getValue()));
							}
							else if (attribute.getName().toString().equals(EA)) {
								reaction.Ea = (Integer.parseInt(attribute.getValue()));
							}
						}
						event = eventReader.nextEvent();
						reactions.add(reaction);
						continue;
					}
					
				}
				// If we reach the end of an ConfigurationInput element we add it to the list
				if (event.isEndElement()) {
					EndElement endElement = event.asEndElement();
					
					if (endElement.getName().getLocalPart() == (INPUT)) {
						
						configurationInputs.add(configurationInput);
					}
					
					if (endElement.getName().getLocalPart() == (EXPERIMENTAL_DATABASES)) {
						configurationInput.experiments.exp_db = list_expdb.toArray(new ExperimentalDatabaseInput[list_expdb.size()]);
					}
					
					if (endElement.getName().getLocalPart() == (REACTOR_SETUPS)) {
						configurationInput.reactor_setup = list_reactor_setup.toArray(new ReactorSetupInput[list_reactor_setup.size()]);
						
						/* Once all the reactor setup input are collected, transform them
						* into reactor inputs and 
						* create reactor input files, if necessary.
						*
						*/
						for(ReactorSetupInput input : configurationInput.reactor_setup)
							configurationInput.addReactorInput(input);
						
					}
					
					if (endElement.getName().getLocalPart() == (REACTIONS)) {
						configurationInput.fitting.optimizedReactions = reactions;
						configurationInput.setParameters();
					}
				}

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return configurationInputs;
	}

}
