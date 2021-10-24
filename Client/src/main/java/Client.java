import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        File fileToDownload;
        InetAddress address = InetAddress.getByName("127.0.0.1");
        int port = 8080;

        if (args.length == 1 || args.length == 3) {
            String path = args[0];
            File currentDirectory = new File(new File("").getAbsolutePath());

            if(!(new File(path)).isAbsolute()) {
                if(!path.startsWith("\\")) {
                    path = "\\" + path;
                }
                path = currentDirectory.getAbsolutePath() + path;
            }

            fileToDownload = new File(path);
            System.out.println(path);
            if(!fileToDownload.isFile()) {
                System.out.println("There is no such file");
                return;
            }

            if (args.length == 3) {
                address = InetAddress.getByName(args[1]);
                port = Integer.parseInt(args[2]);
            }
        }
        else {
            System.out.println("Wrong number of arguments");
            return;
        }

        Socket socket = new Socket(address, port);

        MyFTProtocol.sendFile(socket, fileToDownload);

        if(MyFTProtocol.isSentSuccessful(socket)) {
            System.out.println("Sent was successful");
        }
        else {
            System.out.println("Sent was failed");
        }
    }
}