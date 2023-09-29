package biz.karms.fuzz;


import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.multipart.MultipartFormDataInput;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

@Path("/fruits")
public class FruitResource {

    private final Set<Fruit> fruits = Collections.newSetFromMap(Collections.synchronizedMap(new LinkedHashMap<>()));

    @GET
    public Response list() {
        return Response.ok().entity(fruits).build();
    }

    @GET
    @Path("/id/{id}")
    public Response getById(BigInteger id) {
        return Response.ok()
                .entity(fruits.stream().filter(f -> f.id.equals(id))).build();
    }

    @POST
    public Response add(Fruit fruit) {
        fruits.add(fruit);
        return Response.accepted().entity(this.fruits).build();
    }

    @POST
    @Path("/all")
    public Response addAll(List<Fruit> fruits) {
        this.fruits.addAll(fruits);
        return Response.accepted().entity(this.fruits).build();
    }

    @DELETE
    public Response delete(Fruit fruit) {
        fruits.removeIf(existingFruit -> existingFruit.name.contentEquals(fruit.name));
        return Response.accepted().entity(this.fruits).build();
    }

    @DELETE
    @Path("/all")
    public Response deleteAll() {
        this.fruits.clear();
        return Response.accepted().entity(this.fruits).build();
    }

    @DELETE
    @Path("/id/{id}")
    public Response deleteById(BigInteger id) {
        fruits.removeIf(f -> f.id.equals(id));
        return Response.accepted().entity(this.fruits).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/to/{format}/{filename}")
    public Response image(MultipartFormDataInput data, @PathParam("format") String format, @PathParam("filename") String filename) throws IOException {
        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream();
             final InputStream bin = data.getValues().get("image").stream().toList().get(0).getFileItem().getInputStream()) {
            String colorSpaceName = null;
            String compressionName = null;
            // There are image readers only for jpg, tiff, bmp, gif, wbmp and png.
            final String extension = filename.substring(filename.lastIndexOf('.') + 1);
            if ("jp2".equals(extension)) {
                final BufferedImage image = ImageIO.read(bin);
                colorSpaceName = image.getColorModel().getColorSpace().toString();
                ImageIO.write(image, format, bos);
            } else {
                final ImageReader imageReader = ImageIO.getImageReadersByFormatName(extension).next();
                imageReader.setInput(
                        ImageIO.createImageInputStream(bin),
                        true);
                // Reads both image data and metadata. It exposes code paths in e.g. TIFF plugin.
                final IIOImage iioimg = imageReader.readAll(0, imageReader.getDefaultReadParam());
                colorSpaceName = iioimg.getRenderedImage().getColorModel().getColorSpace().toString();
                final IIOMetadataNode root = (IIOMetadataNode) iioimg.getMetadata()
                        .getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
                // To read some attributes:
                final IIOMetadataNode compressionNode = (IIOMetadataNode) root.getElementsByTagName("CompressionTypeName")
                        .item(0);
                if (compressionNode != null) {
                    compressionName = compressionNode.getAttribute("value");
                }
                ImageIO.write(iioimg.getRenderedImage(), format, bos);
            }
            return Response
                    .accepted()
                    .type(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .entity(bos.toByteArray())
                    .header("color-space", colorSpaceName)
                    .header("compression", compressionName)
                    .build();
        }
    }
}
