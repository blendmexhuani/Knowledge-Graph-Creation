package id.semantics.carml.helper;

import com.taxonic.carml.engine.function.FnoFunction;
import com.taxonic.carml.engine.function.FnoParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CarmlFunctions {

    public static final String FUNCTION_NS = "http://semantics.id/ns/function#";
    public static final String PARAM_NS = "http://semantics.id/ns/parameter#";

    /**
     * Split by comma -> URLs
     *
     * @param data
     * @param ns
     *
     * @return list of string URLs
     */
    @FnoFunction(FUNCTION_NS + "splitToURL")
    public List<String> splitToURL(
            @FnoParam(PARAM_NS + "data") String data,
            @FnoParam(PARAM_NS + "ns") String ns) {
    	if (data == null) return null;
        List<String> values = Arrays.asList(data.split("\\s*,\\s*"));
        values = values.stream().map(value -> ns + value.trim()).collect(Collectors.toList());
        return values;
    }
    
}
