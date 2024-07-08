import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient {
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Chat Client");
    private JTextField textField = new JTextField(40);
    private JTextArea messageArea = new JTextArea(8, 40);

    public ChatClient(String serverAddress) throws IOException {
        // 设置窗口组件
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        // 添加文本框的事件监听

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });

        // 连接到服务器
        Socket socket = new Socket(serverAddress, 12345);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // 启动新的线程来读取消息
        new Thread(new IncomingReader()).start();
    }

    private class IncomingReader implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    messageArea.append(message + "\n");
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient("localhost");
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.textField.setEditable(true);
    }
}
