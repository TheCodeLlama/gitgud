// Test Case 2: Memory Bomb
// Should be killed by 256MB memory limit
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Attempting to allocate excessive memory...");
        List<byte[]> memoryEater = new ArrayList<>();
        try {
            while (true) {
                // Allocate 10MB chunks
                memoryEater.add(new byte[10 * 1024 * 1024]);
                System.out.println("Allocated 10MB...");
            }
        } catch (OutOfMemoryError e) {
            System.err.println("Out of memory!");
        }
    }
}
