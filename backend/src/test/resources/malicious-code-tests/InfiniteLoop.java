// Test Case 1: Infinite Loop
// Should be terminated by timeout (5 seconds)
public class Main {
    public static void main(String[] args) {
        System.out.println("Starting infinite loop...");
        while (true) {
            // This should be killed by timeout
        }
    }
}
