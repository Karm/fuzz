package biz.karms.fuzz;


import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

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
}
