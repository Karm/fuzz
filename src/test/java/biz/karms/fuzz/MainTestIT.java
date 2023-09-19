package biz.karms.fuzz;

import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.main.QuarkusMainIntegrationTest;

@QuarkusMainIntegrationTest
@TestProfile(Port8080.class)
public class MainTestIT extends MainTest {
}
