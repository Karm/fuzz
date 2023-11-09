package biz.karms.fuzz;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Scanner;
import java.util.stream.Stream;

import static biz.karms.fuzz.Util.BASE_DIR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusMainTest
public class MainTest {
    private static final Logger LOG = Logger.getLogger(MainTest.class);
    public static final Path CORPUS_DIR = Path.of(BASE_DIR, "target", "corpus");

    @BeforeAll
    public static void setup() throws IOException {
        if (Files.exists(CORPUS_DIR)) {
            try (final Stream<Path> walk = Files.walk(CORPUS_DIR)) {
                walk.filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } else {
            Files.createDirectories(CORPUS_DIR);
        }
    }

    @Test
    public void readImages(QuarkusMainLauncher launcher) throws IOException {
        try (final Scanner sc = new Scanner(
                new File(Objects.requireNonNull(MainTest.class.getResource("/list.txt")).getFile()), UTF_8)) {
            final StringBuilder sb = new StringBuilder();
            while (sc.hasNextLine()) {
                final String[] lineSegs = sc.nextLine().split("\\|");
                final String filename = lineSegs[0].trim();
                final int width = Integer.parseInt(lineSegs[1].trim());
                final int height = Integer.parseInt(lineSegs[2].trim());
                if (filename.startsWith("#")) {
                    LOG.info("Skipping " + filename);
                    continue;
                }
                LOG.info("Testing " + filename);
                final long start = System.currentTimeMillis();
                final LaunchResult result = launcher.launch("-v", BASE_DIR + "/src/test/resources/" + filename);
                final long end = System.currentTimeMillis();
                result.echoSystemOut();
                assertEquals(0, result.exitCode(), "File: " + filename);
                assertTrue(result.getOutputStream().stream().anyMatch(f -> f.contains("w: " + width + ", h: " + height)),
                        "Expected image size w: " + width + ", h: " + height + " not found " + filename);
                sb.append(filename).append(" done in ").append(end - start).append(" ms\n");
                Files.copy(Path.of(BASE_DIR, "src", "test", "resources", filename), Path.of(CORPUS_DIR.toString(), filename));
            }
            System.out.println(sb);
        }
    }
}
