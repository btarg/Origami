package io.github.btarg.resourcepack;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.github.btarg.OrigamiMain;
import lombok.Getter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

public class ResourcePackServer {

    @Getter
    private static boolean isRunning = false;

    public static void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/" + FileUtils.currentSHA1(), new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        OrigamiMain.setHostingPack(true);
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {

            String pathFromConfig = (String) OrigamiMain.config.get("resource-packs.zipped-resource-pack-path");
            if (pathFromConfig == null || pathFromConfig.isEmpty()) {
                String response = "File not found " + pathFromConfig;
                t.sendResponseHeaders(404, response.length());
                try (java.io.OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            byte[] data = Files.readAllBytes(Path.of(pathFromConfig));
            t.sendResponseHeaders(200, data.length);
            try (java.io.OutputStream os = t.getResponseBody()) {
                os.write(data);
            }
        }
    }

}
