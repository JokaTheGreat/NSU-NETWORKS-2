import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws IOException {
        int port = 8080;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        else if (args.length != 0) {
            System.out.println("Wrong number of arguments");
            return;
        }

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
