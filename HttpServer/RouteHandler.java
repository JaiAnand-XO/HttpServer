
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

public interface RouteHandler {
    void handle(BufferedReader in, OutputStream out) throws IOException;
}
