import java.sql.Connection;
import java.sql.DriverManager;

public class DbTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/scheduleflow";
        String user = "postgres";
        String pass = "nunuhara@11";
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("SUCCESS: Connection to PostgreSQL established.");
        } catch (Exception e) {
            System.out.println("FAILURE: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
