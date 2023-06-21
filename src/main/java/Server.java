import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

@Data
public class Server {
    private final int port;
    private final BooleanSearchEngine searchEngine;

    void start() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream())
                ) {
                    String word = in.readLine();
                    System.out.println("Search request: " + word);

                    out.println(gson.toJson(searchEngine.search(word)));
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать");
            e.printStackTrace();
        }
    }
}
