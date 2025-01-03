package threadPool;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

public class Main {
    private static final String FILE_PATH = "./threadPool/book-war-and-peace.txt";
    private static final int NUM_OF_THREADS = 1;

    public static void main(String[] args) throws IOException {
        String text = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
        startServer(text);   
    }

    private static void startServer(String text) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/search", new WordCountHandler(text));
        Executor executer = Executors.newFixedThreadPool(NUM_OF_THREADS);
        server.setExecutor(executer);
        server.start();
    }

    private static class WordCountHandler implements HttpHandler {
        private String text;

        public WordCountHandler(String text) {
            this.text = text;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String query = httpExchange.getRequestURI().getQuery();
            String[] keyValue = query.split("=");
            String action = keyValue[0];
            String value = keyValue[1];
            if (!action.equals("word")) {
                httpExchange.sendResponseHeaders(400, 0);
                return;
            }

            long count = countWord(value);
            byte[] byteRes = Long.toString(count).getBytes();
            httpExchange.sendResponseHeaders(200, byteRes.length);
            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(byteRes);
            outputStream.close();
        }

        private long countWord(String word) {
            long count = 0;
            int index = 0;
            while(index >= 0) {
                index = text.indexOf(word, index);
                if (index >= 0) {
                    count++;
                    index++;
                }
            }
            return count;
        }
    }
}
