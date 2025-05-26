import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final Map<String, RouteHandler> routes;

    public ClientHandler(Socket socket, Map<String, RouteHandler> routes) {
        this.clientSocket = socket;
        this.routes = routes;
    }

    private Map<String, String> readHeaders(BufferedReader in) throws IOException {
        Map<String, String> headers = new HashMap<>();
        String line;
    
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(":");
            if (colonIndex != -1) {
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                headers.put(key, value);
            }
        }
        return headers;
    }
    

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            OutputStream out = clientSocket.getOutputStream();

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;
            String[] tokens = requestLine.split(" ");
            String method = tokens[0]; // GET or POST
            String path = tokens[1];

            if (routes.containsKey(path)) {
                routes.get(path).handle(in, out);  // handles GET and POST both
            }


            System.out.println("➡️  " + method + " " + path);

            if (routes.containsKey(path)) {
                // Use the registered handler
                routes.get(path).handle(in, out);
            } else {
                // Fallback to static file serving
                serveStaticFile(path, out);
            }

            out.close();
            in.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serveStaticFile(String path, OutputStream out) throws IOException {
        if (path.equals("/")) path = "/index.html";
        File file = new File("public" + path);

        if (file.exists()) {
            byte[] content = Files.readAllBytes(file.toPath());
            String contentType = Files.probeContentType(file.toPath());

            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write(("Content-Type: " + contentType + "\r\n").getBytes());
            out.write(("Content-Length: " + content.length + "\r\n\r\n").getBytes());
            out.write(content);
        } else {
            String body = "<h1>404 Not Found</h1>";
            out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
            out.write("Content-Type: text/html\r\n".getBytes());
            out.write(("Content-Length: " + body.length() + "\r\n\r\n").getBytes());
            out.write(body.getBytes());
        }
    }
}
