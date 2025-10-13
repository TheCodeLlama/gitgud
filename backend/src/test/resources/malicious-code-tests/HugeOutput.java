// Test Case 6: Huge Output
// Should be truncated by output truncation (10KB limit)
public class Main {
    public static void main(String[] args) {
        System.out.println("Generating huge output...");
        // Generate > 10KB of output
        for (int i = 0; i < 1000; i++) {
            // Each line is ~50 bytes, total ~50KB
            System.out.println("This is line " + i + " of massive output to test truncation.");
        }
        System.out.println("Done generating output");
    }
}
