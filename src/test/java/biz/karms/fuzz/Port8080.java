package biz.karms.fuzz;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;

public class Port8080 implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "quarkus.http.port", "8080",
                "quarkus.http.test-port", "8080");
    }

}
