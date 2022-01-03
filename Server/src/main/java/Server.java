import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void start(int port) throws IOException {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(port);

        while(!serverSocket.isClosed()) {
            Socket newConnection = serverSocket.accept();

            threadPool.execute(() -> {
                try {
                    MyFTProtocol.downloadFile(newConnection);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        serverSocket.close();
        threadPool.shutdown();
    }
}
