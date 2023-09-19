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
        if (args.length != 3) {
            System.out.print("""
                    Usage: ./fuzz <port> <-n | -v> <input file>
                           -n: Normal mode
                           -v: Verbose mode
                                        
                    Press Enter to continue...
                    """);
            System.in.read();
            return 0;
        }
        try (final BufferedInputStream f = new BufferedInputStream(Files.newInputStream(Path.of(args[2])));
             final Socket socket = new Socket("localhost", Integer.parseInt(args[0]));
             final BufferedOutputStream o =
                     new BufferedOutputStream(socket.getOutputStream());
             final BufferedInputStream i =
                     new BufferedInputStream(socket.getInputStream())) {
            f.transferTo(o);
            o.flush();
            if (args[1].equals("-v")) {
                // Note that you must not ask for Connection: Keep-Alive in the requests, or the readAllBytes() will hang here.
                System.out.println(new String(i.readAllBytes()));
            } else {
                // We don't care about the output. We just cannot hang up immediately.
                i.read();
            }
        }
        return 0;
    }
}