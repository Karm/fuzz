package biz.karms.fuzz;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

@QuarkusMain
public class Main implements QuarkusApplication {

    @Override
    public int run(String... args) throws Exception {
        if (args.length < 2) {
            System.out.print("""
                    Usage: ./fuzz <-n | -v> <input file>
                           -n: Normal mode
                           -v: Verbose mode
                    Press Enter to continue...
                    """);
            System.in.read();
            return 0;
        }
        final BufferedImage i = ImageIO.read(new File(args[1]));
        if (i != null && "-v".equals(args[0])) {
            System.out.println("w: " + i.getWidth() + ", h: " + i.getHeight());
        }
        return 0;
    }
}
