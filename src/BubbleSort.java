import javafx.concurrent.Task;
import javafx.scene.paint.Color;

/**
 * Educational implementation of Bubble Sort algorithm with enhanced visualization.
 * Bubble Sort repeatedly steps through the list, compares adjacent elements and swaps them if they are in wrong order.
 */
public class BubbleSort {
    // Color scheme for visualization
    private static final Color CURRENT_PAIR_COLOR = Color.web("#f59e0b");     // Amber for current comparison pair
    private static final Color SWAPPED_COLOR = Color.web("#ef4444");          // Red for elements being swapped
    private static final Color SORTED_COLOR = Color.web("#38a169");           // Green for sorted elements
    private static final Color PASS_HIGHLIGHT_COLOR = Color.web("#60a5fa");   // Blue for highlighting current pass

    /**
     * Sorts the array using the Bubble Sort algorithm with enhanced visualization.
     *
     * @param array       the array to sort
     * @param stepByStep  if true, sends animated updates to the visualizer
     * @param visualizer  the VisualizerPanel used to display steps
     * @param task        the current Task for cancellation checking and progress updates
     */
    public static void sort(int[] array, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        int n = array.length;

        // Display the initial array state
        if (stepByStep) {
            visualizer.displayArray(array);
            visualizer.updateStatus("🚀 Starting Bubble Sort - a simple comparison-based algorithm");
            visualizer.highlightPseudocodeLine(0); // Highlight algorithm title
            visualizer.checkPauseAndStep(); // Check if paused
            sleepWithControls(800, visualizer);
            
            // Educational introduction
            visualizer.updateStatus("📚 Bubble Sort compares adjacent elements and swaps them if they're in the wrong order");
            visualizer.checkPauseAndStep(); // Check if paused
            sleepWithControls(1200, visualizer);
        }
        
        boolean swapped;
        int sortedElements = 0;
        
        // Main sorting loop - each pass "bubbles up" the largest element
        for (int i = 0; i < n - 1; i++) {
            if (AlgorithmUtils.isCancelled(task)) return;
            
            swapped = false;
            
            if (stepByStep) {
                visualizer.highlightPseudocodeLine(1); // for i = 0 to n-2
                visualizer.updateStatus("🔄 Starting pass #" + (i + 1) + " through the array");
                // Highlight sorted elements from previous passes
                for (int k = n - sortedElements; k < n; k++) {
                    visualizer.highlightBlock(k, SORTED_COLOR);
                }
                visualizer.checkPauseAndStep(); // Check if paused
                sleepWithControls(600, visualizer);
            }
            
            // Inner loop to perform comparisons and swaps
            for (int j = 0; j < n - i - 1; j++) {
                if (AlgorithmUtils.isCancelled(task)) return;
                
                if (stepByStep) {
                    visualizer.highlightPseudocodeLine(2); // for j = 0 to n-i-2
                    
                    // Highlight the pair being compared
                    visualizer.highlightBlock(j, CURRENT_PAIR_COLOR);
                    visualizer.highlightBlock(j + 1, CURRENT_PAIR_COLOR);
                    
                    visualizer.updateStatus("⚖️ Comparing: " + array[j] + " and " + array[j + 1]);
                    visualizer.checkPauseAndStep(); // Check if paused
                    sleepWithControls(400, visualizer);
                    
                    visualizer.highlightPseudocodeLine(3); // if array[j] > array[j+1]
                }
                
                // Compare adjacent elements
                if (array[j] > array[j + 1]) {
                    if (stepByStep) {
                        visualizer.updateStatus("📊 " + array[j] + " > " + array[j + 1] + ", swapping elements");
                        visualizer.highlightBlock(j, SWAPPED_COLOR);
                        visualizer.highlightBlock(j + 1, SWAPPED_COLOR);
                        visualizer.checkPauseAndStep(); // Check if paused
                        sleepWithControls(500, visualizer);
                        
                        visualizer.highlightPseudocodeLine(4); // swap array[j] and array[j+1]
                    }
                    
                    // Swap elements
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                    swapped = true;
                    
                    if (stepByStep) {
                        visualizer.animateSwap(j, j + 1);
                        visualizer.checkPauseAndStep(); // Check if paused
                        sleepWithControls(600, visualizer);
                    }
                } else if (stepByStep) {
                    visualizer.updateStatus("📊 " + array[j] + " ≤ " + array[j + 1] + ", no swap needed");
                    visualizer.checkPauseAndStep(); // Check if paused
                    sleepWithControls(300, visualizer);
                }
                
                if (stepByStep) {
                    // Reset the color for the current pair
                    visualizer.resetBlockColor(j);
                    if (j < n - i - 2) { // Don't reset the last element yet
                        visualizer.resetBlockColor(j + 1);
                    }
                }
            }
            
            sortedElements++;
            
            // Mark the last element of this pass as sorted
            if (stepByStep) {
                visualizer.highlightBlock(n - i - 1, SORTED_COLOR);
                visualizer.updateStatus("✅ Element " + array[n - i - 1] + " is now in its final sorted position");
                visualizer.checkPauseAndStep(); // Check if paused
                sleepWithControls(500, visualizer);
                
                // Show educational message about optimization
                if (!swapped) {
                    visualizer.updateStatus("🔍 No swaps in this pass - array is already sorted! Early termination possible.");
                    visualizer.checkPauseAndStep(); // Check if paused
                    sleepWithControls(1000, visualizer);
                }
            }
            
            // If no swaps were made in a pass, the array is already sorted
            if (!swapped) {
                break;
            }
            
            // Report progress
            AlgorithmUtils.updateProgress(task, i + 1, n - 1);
        }
        
        // Final state - all sorted
        if (stepByStep) {
            visualizer.displayArray(array);
            
            // Highlight the entire array as sorted
            for (int i = 0; i < n; i++) {
                visualizer.highlightBlock(i, SORTED_COLOR);
                sleepWithControls(50, visualizer); // Quick highlight animation
            }
            
            visualizer.updateStatus("✨ Bubble Sort complete! All " + n + " elements are sorted.");
            visualizer.checkPauseAndStep(); // Check if paused
            
            // Final educational message
            sleepWithControls(800, visualizer);
            visualizer.updateStatus("📊 Bubble Sort: Time complexity O(n²), Space complexity O(1). " +
                    "Simple but inefficient for large arrays.");
        }
    }
    
    /**
     * Enhanced sleep method that respects pause/step controls.
     * For use with the new animation control system.
     */
    private static void sleepWithControls(int millis, VisualizerPanel visualizer) {
        // First check if we need to pause for user interaction
        visualizer.checkPauseAndStep();
        
        // Then sleep for the specified time
        AlgorithmUtils.sleep(millis);
    }
}
