package biz.karms.fuzz;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;

@QuarkusTest
public class RESTTest {
    private static final Logger LOG = Logger.getLogger(RESTTest.class);

    @Test
    public void testApp() throws IOException {
        int c = 0;
        try (Scanner sc = new Scanner(
                new File(Objects.requireNonNull(RESTTest.class.getResource("/list.txt")).getFile()), UTF_8)) {
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
                    given()
                            .when()
                            .accept(ContentType.JSON)
                            .contentType(ContentType.JSON)
                            .body(body)
                            .when()
                            .post("/fruits")
                            .then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_ACCEPTED)
                            .body("$.size()", greaterThan(c++));
                    given()
                            .when()
                            .accept(ContentType.JSON)
                            .contentType(ContentType.JSON)
                            .when()
                            .delete("/fruits/id/" + notUniqueid)
                            .then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_ACCEPTED)
                            .body("$.size()", lessThan(c--));
                    given()
                            .when()
                            .accept(ContentType.JSON)
                            .contentType(ContentType.JSON)
                            .body("[" + body + "," + body + "]")
                            .when()
                            .post("/fruits/all")
                            .then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_ACCEPTED)
                            .body("$.size()", greaterThanOrEqualTo((c = c + 2)));
                    given()
                            .when()
                            .accept(ContentType.JSON)
                            .contentType(ContentType.JSON)
                            .when()
                            .get("/fruits/id/" + notUniqueid)
                            .then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_OK)
                            .body("[0].id", is(notUniqueid));
                    given()
                            .when()
                            .accept(ContentType.JSON)
                            .contentType(ContentType.JSON)
                            .when()
                            .get("/fruits")
                            .then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_OK)
                            .body("$.size()", greaterThanOrEqualTo(c));
                    given()
                            .when()
                            .accept(ContentType.JSON)
                            .contentType(ContentType.JSON)
                            .when()
                            .delete("/fruits/all")
                            .then()
                            .assertThat()
                            .statusCode(HttpStatus.SC_ACCEPTED)
                            .body("$.size()", is(c = 0));
                }
            }
        }
    }
}
