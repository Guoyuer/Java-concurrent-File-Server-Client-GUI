import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Map;

class ReceiverWorker implements Runnable {

    // 为连入的客户端打开的套接口
    private Socket socket;
    private Map<String, Info> states;
    private DataInputStream dataInputStream = null;
    private FileOutputStream fileOutputStream = null;

    ReceiverWorker(Socket socket, Map<String, Info> states) {
        this.socket = socket;
        this.states = states;
    }

    private String getFormattedSize(long size) {
        //获取到的size为：1705230
        int GB = 1024 * 1024 * 1024;//定义GB的计算常量
        int MB = 1024 * 1024;//定义MB的计算常量
        int KB = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize;
        if (size / GB >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) GB) + "GB";
        } else if (size / MB >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) MB) + "MB";
        } else if (size / KB >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) KB) + "KB";
        } else {
            resultSize = size + "B";
        }
        return resultSize;
    }

    @Override
    public void run() {
        try {
            System.out.println("新ReceiverWorker开始运行");
            dataInputStream = new DataInputStream(socket.getInputStream());
            String fileName = dataInputStream.readUTF();
            long fileLength = dataInputStream.readLong();
            File directory = new File("C:\\tmp");
            if (!directory.exists()) {
                directory.mkdir();
            }
            String fullFileName = directory.getAbsolutePath() + File.separatorChar + fileName;
            File file = new File(fullFileName);
            fileOutputStream = new FileOutputStream(file);

            // 开始接收文件
            byte[] bytes = new byte[1024];
            int length = 0;
            long lengthSent = 0;
            long lastLengthSent = 0;
            int ratio;
            long lastRatio = 0;
            long lastTime = 0, currentTime;
            String speed = "0B / s"; //对小于1024B的文件如此显示。
            while ((length = dataInputStream.read(bytes, 0, bytes.length)) != -1) {
                fileOutputStream.write(bytes, 0, length);
                fileOutputStream.flush();
                lengthSent += length;

                ratio = (int) (100 * lengthSent / fileLength);
                if (ratio != lastRatio) { //&& ratio % 5 == 0
                    currentTime = System.currentTimeMillis();
                    if (currentTime != lastTime)
                        speed = getFormattedSize(1000 * (lengthSent - lastLengthSent) / (currentTime - lastTime)) + "/ s";
                    lastLengthSent = lengthSent;
                    lastTime = currentTime;
                    states.put(fullFileName, new Info(ratio, speed));
                    lastRatio = ratio;
                }
            }
            states.put(fullFileName, new Info(100, "0B"));
            Thread.sleep(300);
            states.remove(fullFileName);
            System.out.println("======== 文件接收成功 [File Name：" + fileName + "] [Size：" + (getFormattedSize(fileLength)) + "] ========");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dataInputStream != null) dataInputStream.close();
                if (fileOutputStream != null) fileOutputStream.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}