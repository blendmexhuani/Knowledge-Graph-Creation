package id.semantics.carml.carml;

import com.taxonic.carml.engine.RmlMapper;
import com.taxonic.carml.logical_source_resolver.XPathResolver;
import com.taxonic.carml.model.TriplesMap;
import com.taxonic.carml.util.RmlMappingLoader;
import com.taxonic.carml.vocab.Rdf;
import id.semantics.carml.helper.CarmlFunctions;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.*;
import java.util.Set;

import static id.semantics.carml.helper.Config.TEMP_FILE;
import static id.semantics.carml.helper.Config.TEMP_FILE_SUFFIX;

public class XMLParser {

    /**
     * transform JSON file with caRML and RML mappings, and return Jena Model
     *
     * @param xmlFileName
     * @param rmlFile
     * @return
     * @throws IOException
     */
    public static Model parse(String xmlFileName, String rmlFile) throws IOException {

    	System.out.println("starting XML parsing of " + xmlFileName);

        // create a temp file
        File file = File.createTempFile(TEMP_FILE, TEMP_FILE_SUFFIX);
        file.deleteOnExit();

        // parse xml file
        parse(xmlFileName, rmlFile, file.getPath());

        // create jena model
        Model model = ModelFactory.createDefaultModel();
        InputStream tempInput = new FileInputStream(file);
        RDFDataMgr.read(model, tempInput, Lang.TURTLE);
        tempInput.close();

        return model;
    }

    /**
     * transform XML file with caRML and RML mappings.
     *
     * @param xmlFileName
     * @param rmlFile
     * @param outputFile
     * @return
     * @throws IOException
     */
    public static void parse(String xmlFileName, String rmlFile, String outputFile) throws IOException {

        // load RML file and all supporting functions
        InputStream is = new FileInputStream(rmlFile);
        Set<TriplesMap> mapping = RmlMappingLoader.build().load(RDFFormat.TURTLE, is);
        RmlMapper mapper = RmlMapper.newBuilder().setLogicalSourceResolver(Rdf.Ql.XPath, new XPathResolver())
                .addFunctions(new CarmlFunctions()).build();

        // load input file and convert it to RDF
        InputStream instances = new FileInputStream(xmlFileName);
        mapper.bindInputStream(instances);

        // write it out to an turtle file
        org.eclipse.rdf4j.model.Model sesameModel = mapper.map(mapping);
        is.close();
        instances.close();

        // write to file
        OutputStream tempOutput = new FileOutputStream(outputFile);
        Rio.write(sesameModel, tempOutput, RDFFormat.TURTLE); // write mapping
        sesameModel.clear();
        tempOutput.flush();
        tempOutput.close();
    }
}
