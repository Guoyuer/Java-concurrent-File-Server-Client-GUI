import org.w3c.dom.Text;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

public class FileReceiver {


    LinkedList<Row> rows = new LinkedList<>();
    //    Stack<Row> rows = new Stack<>();
//
    private ConcurrentSkipListMap<String, Info> states = new ConcurrentSkipListMap<>();

    private class Row {
        private TextField text;
        private JProgressBar progressBar;

        Row(TextField text, JProgressBar progressBar) {
            this.text = text;
            this.progressBar = progressBar;
        }
    }

    private JFrame frame;
    private JScrollPane scrollPane;

    FileReceiver() {

    }

    //    private Map<String, Integer> states = new LinkedHashMap<>();

    class StateReader implements Runnable {//读state的同时，设置GUI
        private Map<String, Info> state;
        private JScrollPane scrollPane;
        private JPanel jPanel;

        StateReader(Map<String, Info> states, JScrollPane scrollPane) {
            this.state = states;
            this.scrollPane = scrollPane;
            this.jPanel = (JPanel) scrollPane.getViewport().getComponent(0);

        }

        @Override
        public void run() {
//            this.state = states;
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    e.printStackTrace();
                }


                while (state.size() < jPanel.getComponentCount() / 2) {
                    jPanel.remove(0);
                    jPanel.repaint();
                    jPanel.remove(0);
                    jPanel.repaint();
                    rows.remove(0);
                    System.out.println("Remove!");
                }

                while (state.size() > jPanel.getComponentCount() / 2) {
                    TextField text = new TextField("空闲");
                    text.setPreferredSize(new Dimension(390, 290));
                    JProgressBar progressBar = new JProgressBar(0, 0, 100);
                    progressBar.setPreferredSize(new Dimension(390, 290));
                    progressBar.setStringPainted(true);
                    jPanel.add(text);
                    jPanel.add(progressBar);
                    rows.add(new Row(text, progressBar));
                    System.out.println("Add!");
                    System.out.println(jPanel.getComponentCount());
                }
                jPanel.setLayout(new GridLayout(state.size(), 2, 10, 10));
                jPanel.revalidate();
                scrollPane.revalidate();

                int i = 0;
                for (Map.Entry<String, Info> entry : state.entrySet()) {
                    rows.get(i).text.setText(entry.getKey());
                    rows.get(i).progressBar.setValue(entry.getValue().ratio);
                    rows.get(i).progressBar.setString(entry.getValue().speed);
                    i++;
                }

            }
        }
    }


    public static void main(String[] args) throws IOException {

        FileReceiver receiver = new FileReceiver();

        receiver.launch();
    }


    void launch() throws IOException {
        frame = new JFrame();
        frame.setTitle("多线程接收服务器");
        JPanel jPanel = new JPanel();
        scrollPane = new JScrollPane();
        scrollPane.setViewportView(jPanel);
        scrollPane.setVerticalScrollBar(new JScrollBar());
        frame.add(scrollPane);
        frame.setBounds(300, 200, 800, 600);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        (new Thread(new AddContent(scrollPane))).start();
        (new Thread(new StateReader(states, scrollPane))).start();
        int portNumber = 12001;
        System.out.println("多线程文件接收服务器启动！");
        try (ServerSocket listenSocket = new ServerSocket(portNumber, 10, InetAddress.getByName("127.0.0.1"))) {
            while (true) {
                Socket clientSocket = listenSocket.accept();
                //打开sock输入输出流
//                states.put("122",1);
                (new Thread(new ReceiverWorker(clientSocket, states))).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}