package biz.karms.fuzz;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

@QuarkusMain
public class Main implements QuarkusApplication {

    @Override
    public int run(String... args) throws Exception {
        if (args.length != 2) {
            System.out.print("""
                    Usage: ./fuzz <-n | -v> <input file>
                           -n: Normal mode
                           -v: Verbose mode
                                        
                    Press Enter to continue...
                    """);
            System.in.read();
            return 0;
        }
        try (final BufferedInputStream f = new BufferedInputStream(Files.newInputStream(Path.of(args[1])));
             final Socket socket = new Socket("localhost", 8080);
             final BufferedOutputStream o =
                     new BufferedOutputStream(socket.getOutputStream());
             final BufferedInputStream i =
                     new BufferedInputStream(socket.getInputStream())) {
            f.transferTo(o);
            o.flush();
            if (args[0].equals("-v")) {
                // Note that you must not ask for keep-alive in the requests.
                System.out.println(new String(i.readAllBytes()));
            } else {
                // We don't care about the output. We just cannot hang up immediately.
                i.read();
            }
        }
        return 0;
    }
}