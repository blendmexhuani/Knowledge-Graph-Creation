package id.semantics.carml;

import id.semantics.carml.carml.CSVParser;
import id.semantics.carml.carml.JSONParser;
import id.semantics.carml.carml.XMLParser;
import id.semantics.carml.helper.Config;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

	public static void main(String[] args) throws IOException {
		Model model = loadRdfFile(Config.ONTOLOGY); // load and extend the initial ontology
		int i = -1;
		boolean skipWhile = false;
		while (i != 0) {
			Scanner input = new Scanner(System.in);
			System.out.print("Please press 1 to populate the ontology with instances (0 to exit): ");
			try {
				i = input.nextInt();
			} catch (Exception e) {
				System.out.println("Please enter a number!");
				continue;
			}
			if (i == 1) {
				System.out.println("\nTask 1 process started");
				long start = System.currentTimeMillis();
				processInput(Config.INPUT_FOLDER, Config.OUTPUT_FOLDER, Config.RML_FOLDER);
				model = mergeOutput(model, Config.OUTPUT_FOLDER, Config.OUTPUT_FILE);
				System.out.println("Task 1 process is done in '" + (System.currentTimeMillis() - start) / 1000 + "' seconds");
				String stringOperations = "\nList of predefined queries:\n"
						+ "1. For each person, set rdf:type to Actor/Writer/MovieDirector if he is actor/writer/movie director.\n"
						+ "2. For each actor, return movies in which the actor playsIn.\n"
						+ "3. For each writer, return movies which the writer wrote.\n"
						+ "4. For each director, return movies which the director directed.\n"
						+ "5. For each film studio, return the total number of movies created.\n"
						+ "6. For each genre, return the total number of movies created.\n";
				System.out.println(stringOperations);
				i = -1;
				while (i != 0) {
					input = new Scanner(System.in);
					System.out.print("Please press 2 to run these SPARQL queries (0 to exit): ");
					try {
						i = input.nextInt();
					} catch (Exception e) {
						System.out.println("Please enter a number!");
						continue;
					}
					if (i == 2) {
						System.out.println("\nTask 2 process started");
						start = System.currentTimeMillis();
						processSPARQL(model, Config.QUERY_FOLDER, Config.OUTPUT_FOLDER, Config.OUTPUT_FILE);
						System.out.println("Task 2 process is done in '" + (System.currentTimeMillis() - start) / 1000 + "' seconds");
						skipWhile = true;
						break;
					} else if (i == 0) {
						break;
					} else {
						System.out.println("Please enter 0 or 2!");
					}
				}
			} else if (i == 0) {
				break;
			} else {
				System.out.println("Please enter 0 or 1!");
			}
			if (skipWhile) {
				break;
			}
		}
		System.out.println("\nProgram exit ...");
	}

	public static Model loadRdfFile(String inputPath) {
		Model model = readOntology(inputPath);
		// Add classes to the initial ontology
		OntClass locationResource = ((OntModel) model).createClass(Config.NS_MOVIE + "Location");
		// Add properties to the initial ontology
		Resource movieResource = model.getResource("http://semantics.id/ns/example/movie#Movie");
		Resource personResource = model.getResource("http://xmlns.com/foaf/0.1/Person");
		Resource filmStudioResource = model.getResource("http://semantics.id/ns/example/movie#FilmStudio");
		Property prop = ((OntModel) model).createResource(Config.NS_MOVIE + "locatedIn", OWL.ObjectProperty).as(Property.class);
		prop.addProperty(RDFS.domain, movieResource);
		prop.addProperty(RDFS.range, locationResource);
		prop = ((OntModel) model).createResource(Config.NS_MOVIE + "releaseYear", OWL.DatatypeProperty).as(Property.class);
		prop.addProperty(RDFS.domain, movieResource);
		prop.addProperty(RDFS.range, XSD.gYear);
		prop = ((OntModel) model).createResource(Config.NS_MOVIE + "hasRating", OWL.DatatypeProperty).as(Property.class);
		prop.addProperty(RDFS.domain, movieResource);
		prop.addProperty(RDFS.range, XSD.decimal);
		prop = ((OntModel) model).createResource(Config.NS_MOVIE + "biography", OWL.DatatypeProperty).as(Property.class);
		prop.addProperty(RDFS.domain, personResource);
		prop.addProperty(RDFS.range, XSD.xstring);
		prop = ((OntModel) model).createResource(Config.NS_MOVIE + "height", OWL.DatatypeProperty).as(Property.class);
		prop.addProperty(RDFS.domain, personResource);
		prop.addProperty(RDFS.range, XSD.decimal);
		prop = ((OntModel) model).createResource(Config.NS_MOVIE + "capital", OWL.DatatypeProperty).as(Property.class);
		prop.addProperty(RDFS.domain, locationResource);
		prop.addProperty(RDFS.range, XSD.xstring);
		prop = ((OntModel) model).createResource(Config.NS_MOVIE + "currency", OWL.DatatypeProperty).as(Property.class);
		prop.addProperty(RDFS.domain, locationResource);
		prop.addProperty(RDFS.range, XSD.xstring);
		prop = ((OntModel) model).createResource(Config.NS_MOVIE + "code", OWL.DatatypeProperty).as(Property.class);
		prop.addProperty(RDFS.domain, locationResource);
		prop.addProperty(RDFS.range, XSD.xstring);
		prop = ((OntModel) model).createResource(Config.NS_MOVIE + "locatedIn", OWL.DatatypeProperty).as(Property.class);
		prop.addProperty(RDFS.domain, filmStudioResource);
		prop.addProperty(RDFS.range, locationResource);
		return model;
	}

	private static OntModel readOntology(String inputFile) {
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF);
		RDFDataMgr.read(ontModel, inputFile);
		return ontModel;
	}

	public static void processInput(String inputPath, String outputPath, String rmlPath) throws IOException {
		File folder = new File(inputPath);
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				String fileName = file.getName();
				System.out.println("\t\"" + fileName + "\" is being processed!");
				String inputFile = inputPath + fileName;
				String rmlType = fileName.split("\\.")[1];
				String rmlFile = rmlPath + fileName.split("\\.")[0] + ".rml.ttl";
				String outputFile = outputPath + fileName.split("\\.")[0] + ".ttl";
				conversion(inputFile, rmlType, rmlFile, outputFile); // start carmlizer
			}
		}
	}

	public static void conversion(String inputFile, String rmlType, String rmlFile, String outputFile)
			throws IOException {
		if (rmlType.equalsIgnoreCase("csv")) {
			CSVParser.parse(inputFile, rmlFile, outputFile);
		} else if (rmlType.equalsIgnoreCase("json")) {
			JSONParser.parse(inputFile, rmlFile, outputFile);
		} else if (rmlType.equalsIgnoreCase("xml")) {
			XMLParser.parse(inputFile, rmlFile, outputFile);
		} else {
			System.out.println(rmlType + " is not yet supported");
		}
	}

	public static Model mergeOutput(Model model, String outputDirectory, String outputFileName) throws IOException {
		File folder = new File(outputDirectory);
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName() != outputFileName) {
				String inputFile = outputDirectory + file.getName();
				OntModel ontModel = readOntology(inputFile);
				model = model.union(ontModel); // merge models
			}
		}
		String outputFile = outputDirectory + outputFileName;
		writeFile(outputFile, model);
		return model;
	}

	public static void writeFile(String outputFile, Model model) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(outputFile);
		RDFDataMgr.write(fos, model, Lang.TTL);
		System.out.println("Model has been exported successfully.");
	}

	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	public static void processSPARQL(Model model, String queryPath, String outputDirectory, String outputFileName)
			throws IOException {
		final String lineSeparator = "###################################################";
		File folder = new File(queryPath);
		File[] files = folder.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				System.out.println("\nExecuting " + file.getName() + " ...");
				try {
					String queryFile = queryPath + file.getName();
					String queryString = readFile(queryFile, Charset.forName("UTF-8"));
					QueryExecution execution = QueryExecutionFactory.create(queryString, model);
					Model resultSet = execution.execConstruct();
					model.add(resultSet); // add resultSet to the model
					System.out.println("\n" + lineSeparator);
					RDFDataMgr.write(System.out, resultSet, Lang.TURTLE);
					System.out.println(lineSeparator);
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
		System.out.println("\nAll output has been inserted successfully into the model.");
		String outputFile = outputDirectory + outputFileName;
		writeFile(outputFile, model);
	}
}
