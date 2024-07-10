import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseTest {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=ChatDB;";
    private static final String DB_USER = "qq11";
    private static final String DB_PASSWORD = "qq11";

    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            if (conn != null) {
                System.out.println("Connected to the database successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        }
    }
}
