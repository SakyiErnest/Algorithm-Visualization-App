import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import java.util.Arrays;

/**
 * Enhanced educational implementation of Binary Search algorithm.
 * Provides rich visualization and explanation of the divide-and-conquer approach.
 */
public class BinarySearch {
    // Enhanced color scheme for better visual understanding
    private static final Color ACTIVE_REGION_COLOR = Color.web("#d1d5db");     // Light gray for active region
    private static final Color LOWER_BOUND_COLOR = Color.web("#3b82f6");       // Blue for lower bound
    private static final Color UPPER_BOUND_COLOR = Color.web("#8b5cf6");       // Purple for upper bound
    private static final Color MIDDLE_COLOR = Color.web("#f59e0b");            // Amber for middle element
    private static final Color ELIMINATED_COLOR = Color.web("#94a3b8");        // Gray for eliminated regions
    private static final Color TARGET_FOUND_COLOR = Color.web("#10b981");      // Green for found element
    private static final Color TARGET_NOT_FOUND_COLOR = Color.web("#ef4444");  // Red for not found
    private static final Color DEFAULT_COLOR = VisualizerPanel.DEFAULT_BLOCK_COLOR; // Default block color

    /**
     * Searches for an element in a (sorted) array using the Binary Search algorithm
     * with enhanced educational visualization.
     *
     * @param array            the array in which to search (must be sorted)
     * @param elementToSearch  the element to search for
     * @param stepByStep       if true, sends animated updates to the visualizer for each step
     * @param visualizer       the VisualizerPanel used to display steps
     * @param task             the current Task for cancellation checking and progress updates
     * @return the index of the element if found, or -1 if not found
     */
    public static int search(int[] array, int elementToSearch, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        // Handle the array sorting if needed
        boolean wasSorted = true;
        if (!isSorted(array)) {
            wasSorted = false;
            int[] original = array.clone(); // Keep a copy of the original array

            if (stepByStep) {
                visualizer.updateStatus("⚠️ Binary Search requires a sorted array. Sorting first...");
                sleep(1000);
            }

            Arrays.sort(array);

            if (stepByStep) {
                visualizer.displayArray(array);
                visualizer.updateStatus("🔄 Original array " + Arrays.toString(original) +
                        " sorted to " + Arrays.toString(array));
                sleep(1200);
            }
        }

        // Initialize variables and display starting state
        int left = 0;
        int right = array.length - 1;
        int comparisons = 0;
        int iterations = 0;

        if (stepByStep) {
            // Display the initial array
            visualizer.displayArray(array);

            // Educational introduction
            visualizer.updateStatus("🚀 Starting Binary Search for element: " + elementToSearch);
            visualizer.highlightPseudocodeLine(0); // Highlight algorithm title in pseudocode
            sleep(1000);

            // Initial explanation of binary search
            String initialMsg = wasSorted ?
                    "📚 Binary Search works on sorted arrays by repeatedly dividing the search interval in half" :
                    "📚 Binary Search requires sorted data and works by repeatedly dividing the search interval in half";
            visualizer.updateStatus(initialMsg);
            sleep(1500);

            // Highlight the search space
            highlightSearchSpace(visualizer, array, left, right);
            sleep(800);

            visualizer.updateStatus("🔍 Search space: Indices " + left + " to " + right +
                    " (Values: " + array[left] + " to " + array[right] + ")");
            sleep(1000);

            visualizer.highlightPseudocodeLine(1); // while left <= right
        }

        // Main binary search loop
        while (left <= right && !task.isCancelled()) {
            iterations++;
            int middle = left + (right - left) / 2; // Use this formula to avoid potential overflow
            comparisons++;

            if (stepByStep) {
                // Reset all blocks to default color
                resetAllBlockColors(visualizer, array);

                // Highlight current search boundaries and middle
                highlightSearchSpace(visualizer, array, left, right);

                // Calculate and show the middle element
                visualizer.highlightPseudocodeLine(2); // mid = (left + right) / 2
                visualizer.updateStatus("📏 Iteration " + iterations + ": Calculating middle index = " +
                        left + " + (" + right + " - " + left + ")/2 = " + middle);
                sleep(800);

                // Highlight middle element
                visualizer.highlightBlock(middle, MIDDLE_COLOR);
                visualizer.highlightPseudocodeLine(3); // if array[mid] == key

                // Show comparison with target
                String comparisonMsg = "⚖️ Comparing middle element (" + array[middle] +
                        ") with search target (" + elementToSearch + ")";
                visualizer.updateStatus(comparisonMsg);
                sleep(1000);

                // Educational message about search space reduction
                int currentSize = right - left + 1;
                int originalSize = array.length;
                int percentRemaining = (currentSize * 100) / originalSize;

                if (iterations > 1) {
                    visualizer.updateStatus("📊 Search space reduced to " + percentRemaining +
                            "% of original (" + currentSize + " of " + originalSize + " elements)");
                    sleep(800);
                }
            }

            // Check if middle element is the target
            if (array[middle] == elementToSearch) {
                if (stepByStep) {
                    // Found the element - highlight it
                    visualizer.highlightBlock(middle, TARGET_FOUND_COLOR);
                    visualizer.highlightPseudocodeLine(4); // return mid

                    String foundMsg = String.format("✅ Target %d found at index %d on iteration %d!",
                            elementToSearch, middle, iterations);
                    visualizer.updateStatus(foundMsg);
                    sleep(1000);

                    // Show educational messages about efficiency
                    int theoreticalMaxIterations = (int)(Math.log(array.length) / Math.log(2)) + 1;
                    visualizer.updateStatus("🔍 Found in " + comparisons + " comparisons (theoretical max: " +
                            theoreticalMaxIterations + " for " + array.length + " elements)");
                    sleep(1000);

                    // Final educational message
                    visualizer.updateStatus("📊 Binary Search efficiency: O(log n) - much faster than Linear Search O(n)");
                    sleep(1500);
                }
                return middle; // Element found
            }
            // Check if the middle element is less than the target
            else if (array[middle] < elementToSearch) {
                if (stepByStep) {
                    visualizer.highlightPseudocodeLine(5); // else if array[mid] < key
                    visualizer.updateStatus("⬆️ Middle value " + array[middle] + " < target " +
                            elementToSearch + " - search in right half");
                    sleep(800);

                    // Highlight the eliminated left half
                    for (int i = left; i <= middle; i++) {
                        visualizer.highlightBlock(i, ELIMINATED_COLOR);
                    }
                    sleep(600);

                    visualizer.highlightPseudocodeLine(6); // left = mid + 1
                }

                // Move to the right half
                left = middle + 1;
            }
            // Middle element is greater than the target
            else {
                if (stepByStep) {
                    visualizer.highlightPseudocodeLine(7); // else
                    visualizer.updateStatus("⬇️ Middle value " + array[middle] + " > target " +
                            elementToSearch + " - search in left half");
                    sleep(800);

                    // Highlight the eliminated right half
                    for (int i = middle; i <= right; i++) {
                        visualizer.highlightBlock(i, ELIMINATED_COLOR);
                    }
                    sleep(600);

                    visualizer.highlightPseudocodeLine(8); // right = mid - 1
                }

                // Move to the left half
                right = middle - 1;
            }

            if (stepByStep) {
                visualizer.updateStatus("🔄 New search range: indices " + left + " to " + right);
                sleep(800);

                // Return to the while loop condition check
                visualizer.highlightPseudocodeLine(1);
            }
        }

        // Element not found
        if (stepByStep && !task.isCancelled()) {
            visualizer.highlightPseudocodeLine(9); // return -1

            // Show "not found" visualization
            resetAllBlockColors(visualizer, array);
            for (int i = 0; i < array.length; i++) {
                visualizer.highlightBlock(i, TARGET_NOT_FOUND_COLOR);
            }

            visualizer.updateStatus("❌ Element " + elementToSearch + " not found after " +
                    iterations + " iterations and " + comparisons + " comparisons");
            sleep(1000);

            // Educational conclusion
            String efficiency = String.format("📚 Even though the element wasn't found, Binary Search " +
                            "was still efficient: only used %d comparisons for %d elements",
                    comparisons, array.length);
            visualizer.updateStatus(efficiency);
            sleep(1200);

            // Final message about time complexity
            visualizer.updateStatus("📊 Binary Search time complexity: O(log n) - allows searching " +
                    "1 million elements in about 20 steps");
        }

        return -1; // Element not found
    }

    /**
     * Resets all blocks to the default color by iterating through the array.
     * This replaces the missing resetAllBlocks() method.
     */
    private static void resetAllBlockColors(VisualizerPanel visualizer, int[] array) {
        for (int i = 0; i < array.length; i++) {
            visualizer.resetBlockColor(i);
        }
    }

    /**
     * Highlights the current search space in the visualization.
     */
    private static void highlightSearchSpace(VisualizerPanel visualizer, int[] array, int left, int right) {
        // Mark left and right boundaries
        visualizer.highlightBlock(left, LOWER_BOUND_COLOR);
        visualizer.highlightBlock(right, UPPER_BOUND_COLOR);

        // Mark the active search region
        for (int i = left + 1; i < right; i++) {
            visualizer.highlightBlock(i, ACTIVE_REGION_COLOR);
        }
    }

    /**
     * Checks if the array is sorted in non-decreasing order.
     */
    private static boolean isSorted(int[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i] < array[i - 1]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Utility method to pause execution for animations.
     */
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}