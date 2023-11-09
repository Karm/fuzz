package biz.karms.fuzz;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@QuarkusMain
public class Main implements QuarkusApplication {

    @ConfigProperty(name = "quarkus.http.port")
    int port;

    @Override
    public int run(String... args) throws Exception {
        if (args.length < 2 || "-l".equals(args[0]) && args.length != 3) {
            System.out.print("""
                    Usage: ./fuzz <-n | -v | -l logfile> <input file>
                           -n: Normal mode
                           -v: Verbose mode
                           -l: Log mode
                    Press Enter to continue...
                    """);
            System.in.read();
            return 0;
        }
        try (final BufferedInputStream f = new BufferedInputStream(Files.newInputStream(Path.of("-l".equals(args[0]) ? args[2] : args[1])));
             final Socket socket = new Socket("localhost", port);
             final BufferedOutputStream o =
                     new BufferedOutputStream(socket.getOutputStream());
             final BufferedInputStream i =
                     new BufferedInputStream(socket.getInputStream())) {
            f.transferTo(o);
            o.flush();
            if ("-v".equals(args[0])) {
                // Note that you must not ask for Connection: Keep-Alive in the requests, or the readAllBytes() will hang here.
                System.out.println(new String(i.readAllBytes()));
            } else if ("-l".equals(args[0])) {
                Files.write(Path.of(args[1]), i.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } else {
                // We don't care about the output. We just cannot hang up immediately.
                i.read();
            }
        }
        return 0;
    }
}
