package biz.karms.fuzz;

import java.awt.image.BufferedImage;

public class Thumbnail {

        public final String format;
        public final BufferedImage thumbnail;

    public Thumbnail(String format, BufferedImage thumbnail) {
        this.format = format;
        this.thumbnail = thumbnail;
    }
}
