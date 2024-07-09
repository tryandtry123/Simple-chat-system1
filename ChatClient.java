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
    private String clientName;

    public ChatClient(String serverAddress, String clientName) {
        this.clientName = clientName;

        // 设置窗口组件
        textField.setEditable(true); // 确保文本字段是可编辑的
        messageArea.setEditable(false); // 确保消息区域不可编辑
        messageArea.setBackground(Color.LIGHT_GRAY); // 设置消息区域的背景颜色
        frame.getContentPane().setBackground(Color.DARK_GRAY); // 设置窗口背景颜色

        // 设置布局
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(textField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);

        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.pack();

        // 添加文本框的事件监听
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(); // 使用 sendMessage 方法替换原来的逻辑
            }
        });

        // 添加发送按钮的事件监听
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage(); // 使用 sendMessage 方法替换原来的逻辑
            }
        });

        // 连接到服务器
        try {
            Socket socket = new Socket(serverAddress, 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // 发送客户端标识符
            out.println(clientName);

            // 启动新的线程来读取消息
            new Thread(new IncomingReader()).start();
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + serverAddress);
            JOptionPane.showMessageDialog(frame, "Unknown host: " + serverAddress, "Connection Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            System.out.println("I/O error when connecting to the server: " + serverAddress);
            JOptionPane.showMessageDialog(frame, "I/O error when connecting to the server: " + serverAddress, "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        String message = textField.getText();
        if (message != null && !message.trim().isEmpty()) {
            if (out != null) {
                out.println(message);
                textField.setText("");
                System.out.println("Message sent: " + message); // 添加日志信息
            } else {
                System.out.println("Cannot send message, PrintWriter is null.");
            }
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
                System.out.println("Error reading message from server.");
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    String clientName = JOptionPane.showInputDialog(
                            null,
                            "Enter your name:",
                            "Client Name",
                            JOptionPane.PLAIN_MESSAGE
                    );
                    if (clientName != null && !clientName.trim().isEmpty()) {
                        ChatClient client = new ChatClient("192.168.1.106", clientName); // 使用正确的IP地址
                        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        client.frame.setVisible(true);
                        client.textField.requestFocusInWindow(); // 确保文本字段获得焦点
                    }
                } catch (Exception e) {
                    System.out.println("Error initializing the chat client.");
                    e.printStackTrace();
                }
            }
        });
    }
}
