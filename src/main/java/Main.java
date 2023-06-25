import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static final int PORT = 8989;


    public static void main(String[] args) throws Exception {
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    String words = in.readLine();
                    out.println(gson.toJson(engine.search(words)));
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу запустить сервер");
            e.printStackTrace();
        }
    }
}