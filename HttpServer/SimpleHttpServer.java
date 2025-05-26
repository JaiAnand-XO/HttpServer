import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleHttpServer {
    private static final Map<String, RouteHandler> routes = new ConcurrentHashMap<>();

    // Reads HTTP headers from the BufferedReader and returns them as a Map
    public static Map<String, String> readHeaders(BufferedReader in) throws IOException {
        Map<String, String> headers = new ConcurrentHashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(':');
            if (idx > 0) {
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                headers.put(key, value);
            }
        }
        return headers;
    }

    public static void main(String[] args) throws IOException {
        // Register your custom routes here
        routes.put("/hello", (in, out) -> {
            String body = "<h1>Hello, Vajra Flame!</h1>";
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write("Content-Type: text/html\r\n".getBytes());
            out.write(("Content-Length: " + body.length() + "\r\n\r\n").getBytes());
            out.write(body.getBytes());
        });

        routes.put("/submit", (in, out) -> {
            Map<String, String> headers = readHeaders(in);
            int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));

            char[] bodyChars = new char[contentLength];
            in.read(bodyChars);
            String requestBody = new String(bodyChars);

            System.out.println("📩 Received POST data: " + requestBody);

            String response = "{\"status\": \"received\"}";
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write("Content-Type: application/json\r\n".getBytes());
            out.write(("Content-Length: " + response.length() + "\r\n\r\n").getBytes());
            out.write(response.getBytes());
        });

        routes.put("/api/time", (in, out) -> {
            String json = "{\"time\": \"" + new Date().toString() + "\"}";
            out.write("HTTP/1.1 200 OK\r\n".getBytes());
            out.write("Content-Type: application/json\r\n".getBytes());
            out.write(("Content-Length: " + json.length() + "\r\n\r\n").getBytes());
            out.write(json.getBytes());
        });

        routes.put("/submit", (in, out) -> {
            Map<String, String> headers = readHeaders(in);
            int contentLength = Integer.parseInt(headers.getOrDefault("Content-Length", "0"));
            char[] bodyChars = new char[contentLength];
            in.read(bodyChars);
            String body = new String(bodyChars);
            String[] parts = body.split("=");
            String usernameRaw = parts.length > 1 ? parts[1] : "unknown";
            String username = URLDecoder.decode(usernameRaw, StandardCharsets.UTF_8);

            System.out.println(" Received username: " + username);
            String responseBody = "<h1>Hello, " + username + "!</h1>";
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + responseBody.length() + "\r\n\r\n" +
                    responseBody;

            out.write(response.getBytes());
        });

        String portStr = System.getenv("PORT");
        int port = (portStr != null) ? Integer.parseInt(portStr) : 8085;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server running on port: " + port);

        while (true) {
            Socket client = serverSocket.accept();
            new Thread(new ClientHandler(client, routes)).start();
        }
    }
}
