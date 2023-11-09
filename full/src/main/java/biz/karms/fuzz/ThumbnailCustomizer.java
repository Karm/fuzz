package biz.karms.fuzz;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;


/**
 * Our custom JSON serializer/deserializer for the Thumbnail class.
 */
@Singleton
public class ThumbnailCustomizer implements ObjectMapperCustomizer {

    @Override
    public int priority() {
        return ObjectMapperCustomizer.DEFAULT_PRIORITY + 100;
    }

    @Override
    public void customize(ObjectMapper mapper) {
        mapper.registerModule(new SimpleModule()
                .addSerializer(Thumbnail.class, new JsonSerializer<>() {
                    @Override
                    public void serialize(Thumbnail t, JsonGenerator jg, SerializerProvider sp) throws IOException {
                        jg.writeStartObject();
                        jg.writeStringField("format", t.format);
                        try (final ByteArrayOutputStream o = new ByteArrayOutputStream()) {
                            ImageIO.write(t.thumbnail, t.format, o);
                            jg.writeStringField("thumbnail", Base64.getEncoder().encodeToString(o.toByteArray()));
                        }
                        jg.writeEndObject();
                    }
                })
                .addDeserializer(Thumbnail.class, new JsonDeserializer<>() {
                    @Override
                    public Thumbnail deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                        p.nextToken();
                        p.nextToken();
                        final String format = p.getValueAsString();
                        p.nextToken();
                        p.nextToken();
                        final String base64Image = p.getValueAsString();
                        final BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64Image)));
                        // nextToken here is an important step. You will get [ object, null, object, null ] for arrays otherwise.
                        p.nextToken();
                        return new Thumbnail(format, image);
                    }
                }));
    }
}
