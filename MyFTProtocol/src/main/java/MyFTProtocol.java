import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MyFTProtocol {
    public static final int BUFFER_SIZE = 4096;

    public static void sendFile(Socket socket, File fileToDownload) throws IOException {
        String filePath = fileToDownload.getAbsolutePath();
        String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);

        OutputStream outputStream = socket.getOutputStream();

        sendFileName(outputStream, fileName);
        sendFileSize(outputStream, fileToDownload.length());
        sendFileContent(outputStream, fileToDownload);
    }

    private static void sendFileName(OutputStream socketOutputStream, String fileName) throws IOException {
        byte[] fileNameInBytes = fileName.getBytes();
        int fileNameLengthInBytes = fileNameInBytes.length;

        socketOutputStream.write(ByteCaster.intToBytes(fileNameLengthInBytes));
        socketOutputStream.write(fileName.getBytes());
    }

    private static void sendFileSize(OutputStream socketOutputStream, long fileSize) throws IOException {
        socketOutputStream.write(ByteCaster.longToBytes(fileSize));
    }

    private static void sendFileContent(OutputStream socketOutputStream, File fileToDownload) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        FileInputStream inputFile = new FileInputStream(fileToDownload.getAbsolutePath());

        do {
            bytesRead = inputFile.read(buffer);
            socketOutputStream.write(buffer, 0, bytesRead);
        } while(bytesRead >= BUFFER_SIZE);
    }

    public static boolean isSentSuccessful(Socket socket) throws IOException {
        byte[] intBuffer = new byte[Integer.BYTES];

        int bytesRead = socket.getInputStream().read(intBuffer, 0, Integer.BYTES);
        if (bytesRead != Integer.BYTES) {
            return false;
        }

        return ByteCaster.bytesToInt(intBuffer) == 1;
    }

    public static void downloadFile(Socket socket) throws IOException, InterruptedException {
        InputStream inputStream = socket.getInputStream();

        String fileName = getFileName(inputStream);
        long fileSize = getFileSize(inputStream);

        String downloadDirectory = makeDownloadDirectory();
        String filePath = downloadDirectory + "\\" + fileName;

        ScheduledExecutorService speedLogerThreadPool = startSpeedLoger(filePath);

        saveFile(inputStream, filePath);
        sendDownloadingResult(socket.getOutputStream(), filePath, fileSize);

        speedLogerThreadPool.awaitTermination(SpeedLoger.UPDATE_PERIOD, TimeUnit.SECONDS);
        speedLogerThreadPool.shutdown();
        socket.close();
    }

    private static String makeDownloadDirectory() {
        File currentDirectory = new File(new File("uploads").getAbsolutePath());
        currentDirectory.mkdir();

        return currentDirectory.getAbsolutePath();
    }

    private static String getFileName(InputStream socketInputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        byte[] intBuffer = new byte[Integer.BYTES];

        socketInputStream.read(intBuffer, 0, Integer.BYTES);
        int nameLength = ByteCaster.bytesToInt(intBuffer);
        socketInputStream.read(buffer, 0, nameLength);

        return new String(buffer, 0, nameLength);
    }

    private static long getFileSize(InputStream socketInputStream) throws IOException {
        byte[] longBuffer = new byte[Long.BYTES];

        socketInputStream.read(longBuffer, 0, Long.BYTES);

        return ByteCaster.bytesToLong(longBuffer);
    }

    private static void saveFile(InputStream socketInputStream, String filePath) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        FileOutputStream outFile = new FileOutputStream(filePath);
        do {
            bytesRead = socketInputStream.read(buffer, 0, BUFFER_SIZE);
            outFile.write(buffer, 0, bytesRead);
        } while(bytesRead >= BUFFER_SIZE);

        outFile.close();
    }

    private static ScheduledExecutorService startSpeedLoger(String filePath) {
        SpeedLoger speedCounter = new SpeedLoger(filePath);

        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);
        scheduledThreadPool.scheduleAtFixedRate(speedCounter, SpeedLoger.UPDATE_PERIOD, SpeedLoger.UPDATE_PERIOD, TimeUnit.SECONDS);

        return scheduledThreadPool;
    }

    private static void sendDownloadingResult(OutputStream socketOutputStream, String filePath, long fileSize) throws IOException {
        File file = new File(filePath);

        if(file.length() == fileSize) {
            socketOutputStream.write(ByteCaster.intToBytes(1), 0, Integer.BYTES);
        }
        else {
            socketOutputStream.write(ByteCaster.intToBytes(0), 0, Integer.BYTES);
        }
    }
}

class ByteCaster {
    public static byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }

    public static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    public static int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getInt();
    }

    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }
}
