import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import java.util.Arrays;

/**
 * Enhanced educational implementation of Insertion Sort algorithm.
 * Provides detailed visualization and explanations of each step.
 */
public class InsertionSort {
    // Color scheme for better visual understanding
    private static final Color SORTED_REGION_COLOR = Color.web("#38a169");    // Green for sorted portion
    private static final Color CURRENT_KEY_COLOR = Color.web("#f59e0b");      // Amber for current key
    private static final Color COMPARING_COLOR = Color.web("#3b82f6");        // Blue for comparison
    private static final Color SHIFTING_COLOR = Color.web("#ef4444");         // Red for shifting
    private static final Color INSERTED_COLOR = Color.web("#8b5cf6");         // Purple for newly inserted

    /**
     * Sorts the array using the Insertion Sort algorithm with enhanced visualization.
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
            visualizer.updateStatus("🚀 Starting Insertion Sort - a simple adaptive sorting algorithm");
            visualizer.highlightPseudocodeLine(0); // Highlight algorithm name in pseudocode
            AlgorithmUtils.sleep(800);

            // Initial education message
            visualizer.updateStatus("📚 Insertion Sort works by building a sorted portion one element at a time");
            AlgorithmUtils.sleep(1200);

            // Mark first element as already sorted
            visualizer.highlightBlock(0, SORTED_REGION_COLOR);
            visualizer.updateStatus("✓ First element [" + array[0] + "] is already sorted");
            visualizer.highlightPseudocodeLine(1); // for i = 1 to n-1
            AlgorithmUtils.sleep(800);
        }

        // Main sorting loop - iterate through unsorted portion
        for (int i = 1; i < n; i++) {
            if (AlgorithmUtils.isCancelled(task)) return;

            int key = array[i];

            if (stepByStep) {
                visualizer.highlightPseudocodeLine(2); // key = array[i]
                // Highlight the key element being inserted
                visualizer.highlightBlock(i, CURRENT_KEY_COLOR);
                visualizer.updateStatus("🔑 Selected key: " + key + " (element at index " + i + ")");
                AlgorithmUtils.sleep(600);

                // Show the sorted vs unsorted regions
                visualizer.updateStatus("🔍 Current state: [0 to " + (i-1) + "] is sorted, [" + i + " to " + (n-1) + "] is unsorted");
                for (int k = 0; k <= i-1; k++) {
                    visualizer.highlightBlock(k, SORTED_REGION_COLOR);
                }
                AlgorithmUtils.sleep(800);

                visualizer.highlightPseudocodeLine(3); // j = i - 1
            }

            int j = i - 1;

            // Set a flag to track if any shifting was done
            boolean didShift = false;

            // Shift elements that are greater than the key
            while (j >= 0 && array[j] > key) {
                if (stepByStep) {
                    visualizer.highlightPseudocodeLine(4); // while j >= 0 and array[j] > key

                    // Show the comparison
                    visualizer.highlightBlock(j, COMPARING_COLOR);
                    visualizer.updateStatus("⚖️ Comparing: " + array[j] + " > " + key + "? Yes, need to shift");
                    AlgorithmUtils.sleep(600);

                    // Prepare for shifting animation
                    visualizer.highlightBlock(j, SHIFTING_COLOR);
                    visualizer.highlightPseudocodeLine(5); // array[j+1] = array[j]
                    visualizer.updateStatus("↔️ Shifting " + array[j] + " from position " + j + " to " + (j + 1));
                }

                // Perform the shift
                array[j + 1] = array[j];

                if (stepByStep) {
                    // Animate the shift
                    visualizer.animateShiftRight(j, j + 1);
                    AlgorithmUtils.sleep(500);

                    if (AlgorithmUtils.isCancelled(task)) return;
                }

                j = j - 1;
                didShift = true;

                if (stepByStep) {
                    visualizer.highlightPseudocodeLine(6); // j = j - 1
                }
            }

            if (stepByStep && j >= 0 && !didShift) {
                // Show the comparison that broke the loop (element not greater than key)
                visualizer.highlightBlock(j, COMPARING_COLOR);
                visualizer.updateStatus("⚖️ Comparing: " + array[j] + " > " + key + "? No, found insertion point");
                AlgorithmUtils.sleep(600);
            }

            // Insert the key at the correct position
            array[j + 1] = key;

            if (stepByStep) {
                visualizer.highlightPseudocodeLine(7); // array[j+1] = key

                // Show the insertion
                visualizer.displayArray(array);
                visualizer.highlightBlock(j + 1, INSERTED_COLOR);

                String message = didShift ?
                        "✅ Inserted " + key + " at position " + (j + 1) + " after shifting larger elements" :
                        "✅ Inserted " + key + " at position " + (j + 1) + " (no shifting needed)";
                visualizer.updateStatus(message);
                AlgorithmUtils.sleep(800);

                // Color the whole sorted portion
                for (int k = 0; k <= i; k++) {
                    visualizer.highlightBlock(k, SORTED_REGION_COLOR);
                }

                // Show progress and educational message about the algorithm's characteristics
                int progress = (i * 100) / (n - 1);
                String timeInfo = "";
                if (i == n/4) timeInfo = " | Best case: O(n), Worst case: O(n²)";
                else if (i == n/2) timeInfo = " | Adaptive: performs better on partially sorted arrays";
                else if (i == 3*n/4) timeInfo = " | In-place: requires O(1) extra space";

                visualizer.updateStatus("🔄 Progress: " + progress + "% - " + (i + 1) +
                        " elements sorted" + timeInfo);

                if (AlgorithmUtils.isCancelled(task)) return;
                AlgorithmUtils.sleep(600);

                // Back to main loop
                visualizer.highlightPseudocodeLine(1);
            }

            // Report progress to task
            AlgorithmUtils.updateProgress(task, i, n-1);
        }

        // Final state - all sorted
        if (stepByStep) {
            visualizer.displayArray(array);

            // Show entire array as sorted
            for (int i = 0; i < n; i++) {
                visualizer.highlightBlock(i, SORTED_REGION_COLOR);
                AlgorithmUtils.sleep(50); // quick highlight animation
            }

            visualizer.updateStatus("✨ Insertion Sort complete! All " + n + " elements are sorted.");

            // Final educational message
            AlgorithmUtils.sleep(800);
            visualizer.updateStatus("📊 Insertion Sort: Time complexity O(n²), Space complexity O(1). " +
                    "Efficient for small or nearly sorted arrays.");
        }
    }
}