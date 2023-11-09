package biz.karms.fuzz;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.DatatypeFeature;
import com.fasterxml.jackson.databind.cfg.DatatypeFeatures;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@QuarkusMain
public class Main implements QuarkusApplication {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public int run(String... args) throws Exception {
        if (args.length < 2) {
            System.out.print("""
                    Usage: ./fuzz <-n | -v> <input file>
                           -n: Normal mode
                           -v: Verbose mode
                    Press Enter to continue...
                    """);
            System.in.read();
            return 0;
        }
        objectMapper.enable(DeserializationFeature.EAGER_DESERIALIZER_FETCH);
        objectMapper.enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS);
        objectMapper.enable(JsonParser.Feature.IGNORE_UNDEFINED);
        objectMapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        final Map m = objectMapper.readValue(new File(args[1]), HashMap.class);
        if (m != null && !m.isEmpty() && "-v".equals(args[0])) {
            System.out.println(m);
        }
        return 0;
    }
}
