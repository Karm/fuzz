package biz.karms.fuzz;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusIntegrationTest
@TestProfile(Port8081.class)
public class RESTTestIT extends RESTTest {
}
