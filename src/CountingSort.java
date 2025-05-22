import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import java.util.Arrays;

/**
 * Educational implementation of Counting Sort algorithm with enhanced visualization.
 * Counting Sort is a non-comparison based sorting algorithm that works well when 
 * the range of input values is not significantly larger than the number of elements.
 */
public class CountingSort {
    // Color scheme for visualization
    private static final Color CURRENT_ELEMENT_COLOR = Color.web("#f59e0b");  // Amber for current element
    private static final Color COUNT_COLOR = Color.web("#8b5cf6");           // Purple for count array values
    private static final Color OUTPUT_COLOR = Color.web("#ec4899");          // Pink for output array
    private static final Color SORTED_COLOR = Color.web("#38a169");          // Green for sorted elements
    private static final Color RANGE_COLOR = Color.web("#60a5fa");           // Blue for min/max range
    
    /**
     * Sorts the array using Counting Sort algorithm with enhanced visualization.
     *
     * @param array       the array to sort
     * @param stepByStep  if true, sends animated updates to the visualizer
     * @param visualizer  the VisualizerPanel used to display steps
     * @param task        the current Task for cancellation checking and progress updates
     */
    public static void sort(int[] array, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        int n = array.length;
        
        if (n == 0) return;
        
        // Display the initial array state
        if (stepByStep) {
            visualizer.displayArray(array);
            visualizer.updateStatus("🚀 Starting Counting Sort - a non-comparison based algorithm");
            visualizer.highlightPseudocodeLine(0); // Highlight algorithm title
            sleep(800);
            
            // Educational introduction
            visualizer.updateStatus("📚 Counting Sort works by counting occurrences of each element and reconstructing the sorted array");
            sleep(1200);
        }
        
        // Find the minimum and maximum values in the array
        int min = array[0];
        int max = array[0];
        
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(1); // Find min and max
            visualizer.updateStatus("🔍 Finding the minimum and maximum values in the array");
            visualizer.highlightBlock(0, RANGE_COLOR);
            sleep(500);
        }
        
        for (int i = 1; i < n; i++) {
            if (task.isCancelled()) return;
            
            if (stepByStep) {
                visualizer.highlightBlock(i, CURRENT_ELEMENT_COLOR);
                sleep(200);
            }
            
            if (array[i] < min) {
                min = array[i];
                if (stepByStep) {
                    visualizer.updateStatus("📉 New minimum found: " + min + " at index " + i);
                    sleep(300);
                }
            }
            if (array[i] > max) {
                max = array[i];
                if (stepByStep) {
                    visualizer.updateStatus("📈 New maximum found: " + max + " at index " + i);
                    sleep(300);
                }
            }
            
            if (stepByStep) {
                visualizer.resetBlockColor(i);
            }
        }
        
        // Create and initialize the count array
        int range = max - min + 1;
        int[] count = new int[range];
        
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(2); // Create count array
            visualizer.updateStatus("📊 Range of values: " + min + " to " + max + ", creating count array of size " + range);
            sleep(800);
            
            visualizer.highlightPseudocodeLine(3); // Initialize count array
            visualizer.updateStatus("🔄 Initializing count array with zeros");
            sleep(600);
        }
        
        // Store the count of each element
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(4); // Store count of each element
            visualizer.updateStatus("🧮 Counting occurrences of each element");
            sleep(600);
        }
        
        for (int i = 0; i < n; i++) {
            if (task.isCancelled()) return;
            
            int countIndex = array[i] - min;
            count[countIndex]++;
            
            if (stepByStep) {
                visualizer.highlightBlock(i, CURRENT_ELEMENT_COLOR);
                visualizer.updateStatus("➕ Element " + array[i] + " found, incrementing count[" + countIndex + "] to " + count[countIndex]);
                sleep(400);
                visualizer.resetBlockColor(i);
            }
            
            // Report progress for phase 1
            updateProgress(task, i, n * 3);
        }
        
        // Display the count array
        if (stepByStep) {
            visualizer.updateStatus("📋 Count array: " + Arrays.toString(count));
            sleep(800);
            
            visualizer.highlightPseudocodeLine(5); // Change count[i] to store position
            visualizer.updateStatus("🔢 Modifying count array to store positions of elements in output");
            sleep(600);
        }
        
        // Change count[i] so that count[i] now contains actual 
        // position of this element in output array
        for (int i = 1; i < range; i++) {
            count[i] += count[i - 1];
            
            if (stepByStep && i % 3 == 0) { // Only show some updates to avoid too many messages
                visualizer.updateStatus("🔄 Updating count[" + i + "] to " + count[i] + " (cumulative sum)");
                sleep(300);
            }
        }
        
        if (stepByStep) {
            visualizer.updateStatus("📋 Modified count array: " + Arrays.toString(count));
            sleep(800);
            
            visualizer.highlightPseudocodeLine(6); // Build the output array
            visualizer.updateStatus("🏗️ Building the output array using the count array");
            sleep(600);
        }
        
        // Build the output array
        int[] output = new int[n];
        
        // To make it stable, we iterate from the end
        for (int i = n - 1; i >= 0; i--) {
            if (task.isCancelled()) return;
            
            int countIndex = array[i] - min;
            output[count[countIndex] - 1] = array[i];
            count[countIndex]--;
            
            if (stepByStep) {
                visualizer.highlightBlock(i, CURRENT_ELEMENT_COLOR);
                visualizer.updateStatus("📥 Placing element " + array[i] + " at position " + (count[countIndex]) + " in output array");
                sleep(500);
                visualizer.resetBlockColor(i);
            }
            
            // Report progress for phase 2
            updateProgress(task, n + (n - i), n * 3);
        }
        
        // Copy the output array to original array
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(7); // Copy output to original array
            visualizer.updateStatus("🔄 Copying output array back to the original array");
            sleep(600);
        }
        
        for (int i = 0; i < n; i++) {
            if (task.isCancelled()) return;
            
            array[i] = output[i];
            
            if (stepByStep) {
                visualizer.displayArray(array);
                visualizer.highlightBlock(i, SORTED_COLOR);
                
                if (i % 3 == 0 || i == n-1) { // Only show some updates
                    visualizer.updateStatus("✅ Copied element " + output[i] + " to position " + i + " in original array");
                    sleep(300);
                }
            }
            
            // Report progress for phase 3
            updateProgress(task, n * 2 + i, n * 3);
        }
        
        // Final state - all sorted
        if (stepByStep) {
            visualizer.displayArray(array);
            
            // Highlight the entire array as sorted
            for (int i = 0; i < n; i++) {
                visualizer.highlightBlock(i, SORTED_COLOR);
                sleep(30); // Quick highlight animation
            }
            
            visualizer.updateStatus("✨ Counting Sort complete! All " + n + " elements are sorted.");
            
            // Final educational message
            sleep(800);
            visualizer.updateStatus("📊 Counting Sort: Time complexity O(n+k), Space complexity O(n+k) " +
                    "where k is the range of input. Efficient for small ranges.");
        }
    }
    
    /**
     * Updates the progress in the task using reflection to access protected method.
     */
    private static void updateProgress(Task<?> task, double current, double total) {
        if (task != null && total > 0) {
            try {
                java.lang.reflect.Method method = Task.class.getDeclaredMethod(
                        "updateProgress", double.class, double.class);
                method.setAccessible(true);
                method.invoke(task, current, total);
            } catch (Exception e) {
                // Silently ignore if progress can't be updated
            }
        }
    }
    
    /**
     * Pauses execution for visualization purposes.
     */
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 