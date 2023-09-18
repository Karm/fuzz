package biz.karms.fuzz;

import java.math.BigInteger;

public class Fruit {

    public String name;
    public String description;
    public BigInteger id;
    public Thumbnail thumbnail;

    public Fruit() {
    }

    public Fruit(String name, String description, BigInteger id, Thumbnail thumbnail) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.thumbnail = thumbnail;
    }
}
