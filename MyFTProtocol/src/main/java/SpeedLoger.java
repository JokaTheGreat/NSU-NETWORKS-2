import java.io.File;

class SpeedLoger implements Runnable {
    public static final int UPDATE_PERIOD = 3;
    public static final int BYTES_IN_KILOBYTE = 1024;

    private long callCounter = 0;
    private long oldFileSize = 0;
    private final String filePath;
    private final String fileName;

    public SpeedLoger(String filePath) {
        this.filePath = filePath;
        fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
    }

    @Override
    public void run() {
        callCounter++;
        File updatedFile = new File(filePath);
        long newFileSize = updatedFile.length();
        long instantSpeed = (newFileSize - oldFileSize) / (UPDATE_PERIOD * callCounter);
        long averageSpeed = newFileSize / (UPDATE_PERIOD * callCounter);

        System.out.println(fileName + " instant download speed: " + instantSpeed / BYTES_IN_KILOBYTE + " kb/s");
        System.out.println(fileName + " average download speed: " + averageSpeed / BYTES_IN_KILOBYTE + " kb/s");

        oldFileSize = newFileSize;
    }
}