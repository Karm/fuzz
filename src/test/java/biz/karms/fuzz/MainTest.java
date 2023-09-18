package biz.karms.fuzz;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Stream;

import static biz.karms.fuzz.Util.BASE_DIR;
import static java.nio.charset.StandardCharsets.UTF_8;
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
    public void testManualLaunch(QuarkusMainLauncher launcher) throws IOException {
        try (final Scanner sc = new Scanner(
                new File(Objects.requireNonNull(MainTest.class.getResource("/list.txt")).getFile()), UTF_8)) {
            int c = 0;
            final StringBuilder report = new StringBuilder();
            while (sc.hasNextLine()) {
                long start = System.currentTimeMillis();
                final String filename = sc.nextLine();
                LOG.info("Testing " + filename);
                final String extension = filename.split("\\.")[1];
                try (final ByteArrayOutputStream o = new ByteArrayOutputStream()) {
                    ImageIO.write(ImageIO.read(Objects.requireNonNull(
                            RESTTest.class.getResourceAsStream("/" + filename))), extension, o);
                    final BigInteger notUniqueid = new BigInteger(256, new Random());
                    final String body = String.format("""
                                    {
                                      "name": "%s",
                                      "description": "%s",
                                      "id": %s,
                                      "thumbnail": {
                                        "format": "%s",
                                        "thumbnail": "%s"
                                      }
                                    }
                                    """,
                            "This is a fruit pic " + filename,
                            "Description text",
                            notUniqueid,
                            extension,
                            Base64.getEncoder().encodeToString(o.toByteArray())
                    );
                    final String request = String.format("""
                            POST /fruits HTTP/1.1
                            Accept: application/json, application/javascript, text/javascript, text/json
                            Content-Type: application/json
                            Content-Length: %d
                            Host: localhost:8080
                            Connection: close
                            User-Agent: Whatever you want
                            Accept-Encoding: gzip,deflate

                            %s""", body.length(), body);

                    final Path file = Path.of(CORPUS_DIR.toString(), "request_" + c++ + ".txt");
                    Files.writeString(file, request);
                    LaunchResult result = launcher.launch("-v", file.toString());
                    result.echoSystemOut();
                    assertEquals(0, result.exitCode(), "File: " + file);
                    report.append(file.getFileName().toString());
                    report.append(" ".repeat(15 - file.getFileName().toString().length()));
                    report.append(System.currentTimeMillis() - start);
                    report.append(" ms\n");
                }
            }
            System.out.println(report);
        }
    }
}
