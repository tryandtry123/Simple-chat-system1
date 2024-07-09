import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatClient {
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Chat Client");
    private JTextField textField = new JTextField(40);
    private JTextArea messageArea = new JTextArea(8, 40);
    private JButton sendButton = new JButton("Send");

    public ChatClient(String serverAddress) throws IOException {
        // 设置窗口组件
        textField.setEditable(true); // 确保文本字段是可编辑的
        messageArea.setEditable(false); // 确保消息区域不可编辑
        messageArea.setBackground(Color.LIGHT_GRAY);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();



        // 添加文本框的事件监听
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(); // 使用 sendMessage 方法替换原来的逻辑
            }
        });





        // 连接到服务器
        Socket socket = new Socket(serverAddress, 12345);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // 启动新的线程来读取消息
        new Thread(new IncomingReader()).start();
    }

    // 添加的 sendMessage 方法
    private void sendMessage() {
        String message = textField.getText();
        if (message != null && !message.trim().isEmpty()) {
            out.println(message);
            textField.setText("");
        }
    }



    private class IncomingReader implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    // 获取当前时间并格式化 - 添加的代码
                    String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
                    messageArea.append(timeStamp + " - " + message + "\n"); // 添加时间戳
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    ChatClient client = new ChatClient("192.168.1.106");
                    client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    client.frame.setVisible(true);
                    client.textField.requestFocusInWindow(); // 确保文本字段获得焦点
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
