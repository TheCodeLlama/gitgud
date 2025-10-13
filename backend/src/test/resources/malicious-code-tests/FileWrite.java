// Test Case 5: File System Write Attempt
// Should be blocked by read-only filesystem (except /tmp)
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Attempting to write to filesystem...");

        // Try to write to root filesystem (should fail)
        try {
            FileWriter writer = new FileWriter("/etc/malicious.txt");
            writer.write("malicious content");
            writer.close();
            System.out.println("Write to /etc succeeded (SECURITY VIOLATION!)");
        } catch (IOException e) {
            System.out.println("Write to /etc blocked (expected): " + e.getMessage());
        }

        // Try to write to /tmp (should succeed but limited by tmpfs size)
        try {
            FileWriter writer = new FileWriter("/tmp/test.txt");
            writer.write("test content");
            writer.close();
            System.out.println("Write to /tmp succeeded (allowed in tmpfs)");
        } catch (IOException e) {
            System.out.println("Write to /tmp failed: " + e.getMessage());
        }
    }
}
