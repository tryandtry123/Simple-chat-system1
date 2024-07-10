import java.io.*;
import java.net.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345; // 服务器端口
    private static final Set<PrintWriter> clientWriters = new HashSet<>(); // 存储所有客户端的输出流
    private static final Map<PrintWriter, String> clientNames = new HashMap<>(); // 存储客户端标识符
    private static final File logFile = new File("chat_log.txt"); // 日志文件

    // SQL Server 数据库连接字符串，使用SQL Server身份验证
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=ChatDB;";
    private static final String DB_USER = "qq11"; // 新创建的用户
    private static final String DB_PASSWORD = "qq11"; // 用户的密码

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running..."); // 输出服务器启动信息
        ServerSocket listener = new ServerSocket(PORT); // 创建服务器套接字，监听指定端口
        try {
            while (true) {
                new Thread(new Handler(listener.accept())).start(); // 为每个新连接启动一个新的处理线程
            }
        } finally {
            listener.close(); // 关闭服务器套接字
        }
    }

    private static class Handler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public Handler(Socket socket) {
            this.socket = socket; // 初始化套接字
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // 获取输入流
                out = new PrintWriter(socket.getOutputStream(), true); // 获取输出流

                // 读取客户端标识符
                String clientName = in.readLine();
                System.out.println("Connected: " + clientName); // 输出连接信息
                synchronized (clientWriters) {
                    clientWriters.add(out); // 将输出流添加到集合
                    clientNames.put(out, clientName); // 将客户端标识符与输出流关联
                }

                String message;
                // 不断读取客户端发送的消息
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message); // 输出接收到的消息
                    String formattedMessage = formatMessageWithTimestamp(clientName, message); // 格式化消息
                    logMessage(formattedMessage); // 保存消息到文件
                    saveMessageToDatabase(clientName, formattedMessage); // 保存消息到数据库（注意这里是格式化后的消息）
                    synchronized (clientWriters) {
                        // 将消息广播给所有客户端
                        for (PrintWriter writer : clientWriters) {
                            writer.println(formattedMessage); // 带有标识符的消息
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error in communication with client: " + e.getMessage()); // 处理通信异常
            } finally {
                try {
                    socket.close(); // 关闭套接字
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage()); // 处理套接字关闭异常
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out); // 从集合中移除输出流
                    clientNames.remove(out); // 从映射中移除标识符
                }
            }
        }

        /**
         * 将消息保存到数据库中
         *
         * @param clientName 发送消息的客户端标识符（用户名）
         * @param message    聊天消息内容
         */
        private void saveMessageToDatabase(String clientName, String message) {
            System.out.println("Saving message to database: " + message); // 输出保存信息调试信息
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO Messages (client_name, message) VALUES (?, ?)")) {
                stmt.setString(1, clientName); // 设置第一个参数为客户端标识符
                stmt.setString(2, message); // 设置第二个参数为消息内容
                stmt.executeUpdate(); // 执行更新操作，将消息插入数据库
                System.out.println("Message saved to database successfully."); // 输出成功信息
            } catch (SQLException e) {
                System.out.println("Error saving message to database: " + e.getMessage()); // 处理SQL异常
            }
        }

        /**
         * 记录消息到日志文件
         *
         * @param message 要记录的消息
         */
        private void logMessage(String message) {
            System.out.println("Logging message: " + message); // 输出日志信息调试信息
            try (FileWriter fw = new FileWriter(logFile, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter pw = new PrintWriter(bw)) {
                pw.println(message); // 写入消息到日志文件
                System.out.println("Message logged to file successfully."); // 输出成功信息
            } catch (IOException e) {
                System.out.println("Error writing to log file: " + e.getMessage()); // 处理IO异常
            }
        }

        /**
         * 格式化消息，添加时间戳
         *
         * @param clientName 发送消息的客户端标识符（用户名）
         * @param message    聊天消息内容
         * @return 带有时间戳的格式化消息
         */
        private String formatMessageWithTimestamp(String clientName, String message) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String timestamp = formatter.format(new java.util.Date());
            return timestamp + " " + clientName + ": " + message;
        }
    }
}
