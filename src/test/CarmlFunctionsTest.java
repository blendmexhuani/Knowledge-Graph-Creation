package test;
import id.semantics.carml.helper.CarmlFunctions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

public class CarmlFunctionsTest {

    private static CarmlFunctions carmlObj;

    @BeforeClass public static void setUp() throws Exception {
        carmlObj = new CarmlFunctions();
    }

    @AfterClass public static void tearDown() throws Exception {
        // do nothing
    }

    @Test public void testSplitToURL() {
        List<String> result = carmlObj.splitToURL("Drama, History, Musical", "http://semantics.id/ns/example/movie#");
        assertEquals(new ArrayList<String>(){
        	/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
        		add("http://semantics.id/ns/example/movie#Drama"); 
        		add("http://semantics.id/ns/example/movie#History"); 
        		add("http://semantics.id/ns/example/movie#Musical");
        	}
        }, result);
    }

    @Test public void testNullSplitToURL() {
        List<String> result = carmlObj.splitToURL("", "http://semantics.id/ns/example/movie#");
        assertEquals(new ArrayList<String>(){
        	/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			{
        		add("http://semantics.id/ns/example/movie#");
        	}
        }, result);
    }
    
}
