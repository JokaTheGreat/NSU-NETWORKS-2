import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }
        else if (args.length != 0) {
            System.out.println("Wrong number of arguments");
            return;
        }

        Server.start(port);
    }
}
