package biz.karms.fuzz;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.apache.commons.lang3.RandomStringUtils;
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
import java.util.SplittableRandom;
import java.util.stream.Stream;

import static biz.karms.fuzz.Util.BASE_DIR;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusMainTest
public class MainTest {
    private static final Logger LOG = Logger.getLogger(MainTest.class);
    public static final Path CORPUS_DIR = Path.of(BASE_DIR, "target", "corpus");
    public static final String SOME_EMOJIS = "❀*ੈ✩‧₊˚✧˚ ༘ ☹#️⃣*️⃣0️⃣1️⃣2️⃣3️⃣4️⃣5️⃣6️⃣7️⃣8️⃣9️⃣©️®️‼️⁉️™️ℹ️↔️↕️↖️↗️↘️↙️↩️↪️⌚⌛⌨️⏏️⏩⏬⏭️⏮️⏯️⏰⏱️⏲️⏳⏸️⏹️⏺️Ⓜ️▪️▫️▶️◀️◻️◼️◽◾☀️☁️☂️☃️☄️☎️☑️☔☕☘️☝️☝☝☝☝☝☠️☢️☣️☦️☪️☮️☯️☸️☹️☺️♀️♂️♈♓♟️♠️♣️♥️♦️♨️♻️♾️♿⚒️⚓⚔️⚕️⚖️⚗️⚙️⚛️⚜️⚠️⚡⚧️⚪⚫⚰️⚱️⚽⚾⛄⛅⛈️⛎⛏️⛑️⛓️⛔⛩️⛪⛰️⛱️⛲⛳⛴️⛵⛷️⛸️⛹️⛹⛹⛹⛹⛹⛺⛽✂️✅✈️✉️✊✋✊✊✊✊✊✋✋✋✋✋✌️✌✌✌✌✌✍️✍✍✍✍✍✏️✒️✔️✖️✝️✡️✨✳️✴️❄️❇️❌❎❓❕❗❣️❤️➕➗➡️➰➿⤴️⤵️⬅️⬆️⬇️⬛⬜⭐⭕〰️〽️㊗️㊙️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️️⋆｡♡˚✧˚ ༘ ⋆｡♡˚";

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
    public void generatePost(QuarkusMainLauncher launcher) throws IOException {
        try (final Scanner sc = new Scanner(
                new File(Objects.requireNonNull(MainTest.class.getResource("/list.txt")).getFile()), UTF_8)) {
            int c = 0;
            final StringBuilder report = new StringBuilder();
            final SplittableRandom rnd = new SplittableRandom();
            while (sc.hasNextLine()) {
                final String filename = sc.nextLine();
                LOG.info("Testing " + filename);
                final String extension = filename.split("\\.")[1];
                try (final ByteArrayOutputStream o = new ByteArrayOutputStream()) {
                    ImageIO.write(ImageIO.read(Objects.requireNonNull(
                            RESTTest.class.getResourceAsStream("/" + filename))), extension, o);
                    final BigInteger notUniqueid = new BigInteger(256, new Random());
                    final String body = String.format("""
                                    {
                                      "name": "%s %s",
                                      "description": "%s %s",
                                      "id": %s,
                                      "thumbnail": {
                                        "format": "%s",
                                        "thumbnail": "%s"
                                      }
                                    }
                                    """,
                            filename,
                            RandomStringUtils.randomAlphanumeric(rnd.nextInt(1, 20)),
                            SOME_EMOJIS.substring(0, rnd.nextInt(1, SOME_EMOJIS.length())),
                            RandomStringUtils.randomAlphanumeric(rnd.nextInt(1, 20)),
                            notUniqueid,
                            extension,
                            Base64.getEncoder().encodeToString(o.toByteArray()));
                    final String request = String.format("""
                            POST /fruits HTTP/1.1
                            Accept: application/json, application/javascript, text/javascript, text/json
                            Content-Type: application/json;charset=utf-8
                            Content-Length: %d
                            Host: localhost:8080
                            Connection: close
                            User-Agent: Whatever you want
                            Accept-Encoding: gzip,deflate

                            %s""", body.getBytes(UTF_8).length, body);

                    // POST
                    final Path file = Path.of(CORPUS_DIR.toString(), "post_" + c + ".txt");
                    Files.writeString(file, request, UTF_8);
                    final long start = System.currentTimeMillis();
                    final LaunchResult result = launcher.launch("8080", "-v", file.toString());
                    final long end = System.currentTimeMillis();
                    result.echoSystemOut();
                    assertEquals(0, result.exitCode(), "File: " + file);
                    assertTrue(result.getOutputStream().stream().anyMatch(f -> f.contains("HTTP/1.1 202 Accepted")),
                            "Missing HTTP/1.1 202 Accepted with file: " + file);
                    report.append(file.getFileName().toString());
                    report.append(" ".repeat(20 - file.getFileName().toString().length()));
                    report.append(end - start);
                    report.append(" ms\n");

                    // DELETE
                    // Note the DELETE would fail as there is no context kept between executions.
                    final String deleteRequest = String.format("""
                            DELETE /fruits/id/%s HTTP/1.1
                            Content-Type: application/json
                            Accept: application/json, application/javascript, text/javascript, text/json
                            Host: localhost:8080
                            Connection: close
                            User-Agent: Apache-HttpClient/4.5.14 (Java/17.0.8)
                            Accept-Encoding: gzip,deflate

                            """, notUniqueid);
                    final Path deleteFile = Path.of(CORPUS_DIR.toString(), "delete_" + c + ".txt");
                    Files.writeString(deleteFile, deleteRequest);
                    final LaunchResult deleteResult = launcher.launch("8080", "-v", deleteFile.toString());
                    deleteResult.echoSystemOut();
                    assertEquals(0, deleteResult.exitCode(), "File: " + deleteFile);
                    assertTrue(deleteResult.getOutputStream().stream().anyMatch(f -> f.contains("HTTP/1.1 202 Accepted")),
                            "Missing HTTP/1.1 202 Accepted with file: " + deleteFile);
                    c++;
                }
            }
            System.out.println(report);
        }

        int split = 3;
        int c = 0;
        try (final Scanner sc = new Scanner(
                new File(Objects.requireNonNull(MainTest.class.getResource("/list.txt")).getFile()), UTF_8)) {
            final StringBuilder report = new StringBuilder();
            final SplittableRandom rnd = new SplittableRandom();
            final StringBuilder jsonArray = new StringBuilder();
            while (sc.hasNextLine()) {
                final String filename = sc.nextLine();
                final String extension = filename.split("\\.")[1];
                try (final ByteArrayOutputStream o = new ByteArrayOutputStream()) {
                    ImageIO.write(ImageIO.read(Objects.requireNonNull(
                            RESTTest.class.getResourceAsStream("/" + filename))), extension, o);
                    final BigInteger notUniqueid = new BigInteger(256, new Random());
                    final String body = String.format("""
                                    {
                                      "name": "%s %s",
                                      "description": "%s %s",
                                      "id": %s,
                                      "thumbnail": {
                                        "format": "%s",
                                        "thumbnail": "%s"
                                      }
                                    }
                                    """,
                            filename,
                            RandomStringUtils.randomAlphanumeric(rnd.nextInt(1, 20)),
                            SOME_EMOJIS.substring(0, rnd.nextInt(1, SOME_EMOJIS.length())),
                            RandomStringUtils.randomAlphanumeric(rnd.nextInt(1, 20)),
                            notUniqueid,
                            extension,
                            Base64.getEncoder().encodeToString(o.toByteArray()));
                    jsonArray.append(body);
                    jsonArray.append(",");
                }
                if (c % split == 0) {
                    jsonArray.setLength(jsonArray.length() - 1);
                    final String body = String.format("[%s]", jsonArray);
                    final String request = String.format("""
                            POST /fruits/all HTTP/1.1
                            Accept: application/json, application/javascript, text/javascript, text/json
                            Content-Type: application/json;charset=utf-8
                            Content-Length: %d
                            Host: localhost:8080
                            Connection: close
                            User-Agent: Whatever you want but something sinister 1.1.1.1.1
                            Accept-Encoding: gzip,deflate

                            %s""", body.getBytes(UTF_8).length, body);
                    final Path file = Path.of(CORPUS_DIR.toString(), "post_all_" + c++ + ".txt");
                    LOG.info("Creating file " + file.toAbsolutePath());
                    Files.writeString(file, request, UTF_8);
                    final long start = System.currentTimeMillis();
                    final LaunchResult result = launcher.launch("8080", "-v", file.toString());
                    final long end = System.currentTimeMillis();
                    result.echoSystemOut();
                    assertEquals(0, result.exitCode(), "File: " + file);
                    assertTrue(result.getOutputStream().stream().anyMatch(f -> f.contains("HTTP/1.1 202 Accepted")),
                            "Missing HTTP/1.1 202 Accepted with file: " + file);
                    report.append(file.getFileName().toString());
                    report.append(" ".repeat(20 - file.getFileName().toString().length()));
                    report.append(end - start);
                    report.append(" ms\n");
                }
            }
            System.out.println(report);
        }
    }
}
