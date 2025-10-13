// Test Case 4: Network Access Attempt
// Should be blocked by --network none
import java.net.URL;
import java.net.HttpURLConnection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Attempting to access network...");
        try {
            URL url = new URL("http://google.com");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.connect();
            System.out.println("Network access succeeded (SECURITY VIOLATION!)");
        } catch (Exception e) {
            System.out.println("Network access blocked (expected): " + e.getMessage());
        }
    }
}
