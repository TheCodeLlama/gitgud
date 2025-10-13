// Test Case 3: Fork Bomb (Process Creation)
// Should be prevented by PID limit (50 processes)
public class Main {
    public static void main(String[] args) {
        System.out.println("Attempting to create many processes...");
        for (int i = 0; i < 100; i++) {
            try {
                ProcessBuilder pb = new ProcessBuilder("sleep", "10");
                Process p = pb.start();
                System.out.println("Created process " + i);
            } catch (Exception e) {
                System.err.println("Failed to create process: " + e.getMessage());
                break;
            }
        }
    }
}
