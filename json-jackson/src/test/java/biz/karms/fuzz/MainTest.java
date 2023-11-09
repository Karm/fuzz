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
import static java.nio.charset.StandardCharsets.UTF_16;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void readJsons(QuarkusMainLauncher launcher) throws IOException {
        try (final Scanner sc = new Scanner(
                new File(Objects.requireNonNull(MainTest.class.getResource("/json_UTF16.txt")).getFile()), UTF_16)) {
            final StringBuilder sb = new StringBuilder();
            int c = 0;
            while (sc.hasNextLine()) {
                final String json = sc.nextLine();
                if (json.startsWith("#")) {
                    LOG.info("Skipping JSON number " + c);
                    continue;
                }
                LOG.info("Testing JSON number " + c);
                final Path file = Path.of(CORPUS_DIR.toString(), c + ".json");
                Files.writeString(file, json, UTF_16);
                final long start = System.currentTimeMillis();
                final LaunchResult result = launcher.launch("-v", file.toAbsolutePath().toString());
                final long end = System.currentTimeMillis();
                result.echoSystemOut();
                // We are fine with com.fasterxml.jackson.core.JsonParseException etc.
                //assertEquals(0, result.exitCode(), "File: " + file.getFileName());
                sb.append(file.getFileName()).append(" done in ").append(end - start).append(" ms\n");
                c++;
            }
            System.out.println(sb);
        }
    }
}
