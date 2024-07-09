import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<PrintWriter, String> clientNames = new HashMap<>(); // 存储客户端标识符
    private static File logFile = new File("chat_log.txt");

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                new Thread(new Handler(listener.accept())).start();
            }
        } finally {
            listener.close();
        }
    }

    private static class Handler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                // 读取客户端标识符
                String clientName = in.readLine();
                synchronized (clientWriters) {
                    clientWriters.add(out);
                    clientNames.put(out, clientName);
                }
                String message;

                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    logMessage(message); // 保存消息到文件
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(clientName + ": " + message); // 广播带有标识符的消息
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                }
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                    clientNames.remove(out);
                }
            }
        }

        // 保存消息到文件的方法
        private void logMessage(String message) {
            try (FileWriter fw = new FileWriter(logFile, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter pw = new PrintWriter(bw)) {
                pw.println(message);
            } catch (IOException e) {
                System.out.println("Error writing to log file: " + e);
            }
        }
    }
}
