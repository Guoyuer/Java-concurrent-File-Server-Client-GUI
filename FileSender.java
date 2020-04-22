import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;


public class FileSender {
    private JFrame frame;
    private JTextArea msgTextArea;
    private JPanel panel;    //创建面板
    private JButton openBtn;
    private JButton sendBtn;
    private File file;


    private void sendFile() throws IOException {
        FileInputStream fileInputStream = null;
        DataOutputStream dataOutputStream = null;
        String hostName = "127.0.0.1";
        int portNumber = 12001;

        try {
            Socket clientSocket = new Socket(hostName, portNumber);
            if (file.exists()) {
                fileInputStream = new FileInputStream(file);
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataOutputStream.writeUTF(file.getName());
                dataOutputStream.flush();
                dataOutputStream.writeLong(file.length());
                dataOutputStream.flush();
                msgTextArea.append("======== 开始传输文件 ========\n");
                byte[] bytes = new byte[1024];

                int length = 0;
                while ((length = fileInputStream.read(bytes, 0, bytes.length)) != -1) {
                    dataOutputStream.write(bytes, 0, length);
                    dataOutputStream.flush();
                }
                msgTextArea.append("======== 文件传送完毕！ ========\n\n\n");

            } else {
                msgTextArea.append("文件不存在!\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) fileInputStream.close();
            if (dataOutputStream != null) dataOutputStream.close();
        }
    }

    private void chooseFile() {
        // 创建一个默认的文件选取器
        JFileChooser fileChooser = new JFileChooser();

        // 设置默认显示的文件夹为当前文件夹
        fileChooser.setCurrentDirectory(new File("."));

        // 设置文件选择的模式（只选文件、只选文件夹、文件和文件均可选）
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // 设置是否允许多选
        fileChooser.setMultiSelectionEnabled(false);

        // 打开文件选择框（线程将被阻塞, 直到选择框被关闭）
        int result = fileChooser.showOpenDialog(frame);

        if (result == JFileChooser.APPROVE_OPTION) {
            // 如果点击了"确定", 则获取选择的文件路径
            file = fileChooser.getSelectedFile();
            msgTextArea.append("选中文件: " + file.getAbsolutePath() + "\n");
            System.out.println(file.hashCode());
            sendBtn.setEnabled(true);
        } else {
            file = null;
            msgTextArea.append("文件选择有误！\n");
        }
    }

    private FileSender() {

        frame = new JFrame("单线程发送端");
        frame.setLayout(new BorderLayout(10, 10));
        msgTextArea = new JTextArea();
        msgTextArea.setSize(250, 500);
        msgTextArea.setLineWrap(true);
        msgTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.add(msgTextArea);
        scrollPane.setSize(250, 500);
        scrollPane.setViewportView(msgTextArea);
        frame.add(scrollPane, BorderLayout.CENTER);

//        panel = new JPanel();    //创建面板
//        panel.setLayout(new BorderLayout(10, 10));
        openBtn = new JButton("打开");
        openBtn.addActionListener(e -> chooseFile());
        frame.add(openBtn, BorderLayout.NORTH);

        sendBtn = new JButton("发送");
        sendBtn.setEnabled(false);
        sendBtn.addActionListener(e -> {
            try {
                sendFile();
            } catch (Exception IOe) {
                IOe.printStackTrace();
            }
        });
        frame.add(sendBtn, BorderLayout.SOUTH);
//        panel.add(openBtn);


        frame.setSize(300, 200);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public static void main(String[] args) {
        FileSender sender = new FileSender();


    }
}


