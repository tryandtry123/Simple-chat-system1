import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JFrame;

public class ChatClientLauncher {

    public static void main(String[] args) {
        String clientName = "YourClientName"; // 你可以根据实际情况动态获取或传递客户端名称

        if (clientName != null && !clientName.trim().isEmpty()) {
            try {
                // 获取本地IP地址
                InetAddress inetAddress = InetAddress.getLocalHost();
                String ipAddress = inetAddress.getHostAddress();

                // 使用获取到的IP地址创建ChatClient对象
                ChatClient client = new ChatClient(ipAddress, clientName);
                client.getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                client.getFrame().setVisible(true);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Client name cannot be null or empty");
        }
    }
}
