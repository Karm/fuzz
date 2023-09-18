package biz.karms.fuzz;

import io.quarkus.runtime.ApplicationLifecycleManager;
import io.quarkus.runtime.Quarkus;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.AfterAll;

import java.util.logging.Logger;

@QuarkusIntegrationTest
public class RESTTestIT extends RESTTest {

    /* Did not help
    private static final Logger LOG = Logger.getLogger(RESTTestIT.class.getName());

    @AfterAll
    public static void teardown() throws InterruptedException {
        LOG.info("Stopping Quarkus");
        ApplicationLifecycleManager.exit(-1);
        Thread.sleep(1000);
    }
*/

}
