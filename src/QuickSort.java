import javafx.concurrent.Task;
import javafx.scene.paint.Color;

public class QuickSort {
    // Constants for visualization - More descriptive names and reuse VisualizerPanel defaults
    private static final Color PIVOT_COLOR = Color.ORANGE;
    private static final Color COMPARING_COLOR = Color.YELLOW; // Consistent naming
    private static final Color LESS_THAN_PIVOT_COLOR = Color.LIGHTGREEN; // More descriptive than SORTED_COLOR for this context
    private static final Color PARTITION_COLOR = Color.PURPLE; // More descriptive than PARTITION_BOUNDARY_COLOR
    private static final Color DEFAULT_COLOR = VisualizerPanel.DEFAULT_BLOCK_COLOR; // Reusing default from VisualizerPanel
    private static final int ANIMATION_DELAY = 500; // Slightly reduce for responsiveness, can be adjusted

    /**
     * Sorts the array using the QuickSort algorithm with enhanced visualization.
     *
     * @param arr the array to sort
     * @param low the starting index of the subarray to sort
     * @param high the ending index of the subarray to sort
     * @param stepByStep if true, sends updates to the visualizer for each step
     * @param visualizer the VisualizerPanel used to display visualization
     * @param task the current Task for cancellation checking
     */
    public static void sort(int[] arr, int low, int high, boolean stepByStep,
                            VisualizerPanel visualizer, Task<?> task) {
        if (low < high && !task.isCancelled()) {
            if (stepByStep) {
                visualizer.updateStatus("Starting QuickSort on subarray [" + low + ".." + high + "]");
                highlightPartitionRange(visualizer, low, high); // More descriptive method name
                sleep(ANIMATION_DELAY);
                if (task.isCancelled()) return; // Early cancellation check
            }

            // Partition the array and get the pivot index
            int pivotIndex = partition(arr, low, high, stepByStep, visualizer, task);

            if (stepByStep) {
                visualizer.updateStatus("Pivot " + arr[pivotIndex] + " placed at index " + pivotIndex + ". " +
                        "Elements to the left are smaller, to the right are larger.");
                visualizer.highlightBlock(pivotIndex, LESS_THAN_PIVOT_COLOR); // Pivot is in its sorted place within partition
                sleep(ANIMATION_DELAY);
                if (task.isCancelled()) return;
            }

            // Recursively sort elements before and after pivot
            sort(arr, low, pivotIndex - 1, stepByStep, visualizer, task); // Sort left subarray
            sort(arr, pivotIndex + 1, high, stepByStep, visualizer, task); // Sort right subarray

            if (stepByStep) {
                resetPartitionHighlight(visualizer, low, high); // Clear partition highlight after sorting subarrays
            }
        } else if (stepByStep && low >= high) {
            // Base case visualization: single element or empty subarray is considered sorted
            visualizer.updateStatus("Subarray [" + low + ".." + high + "] is sorted (base case or empty).");
            if (low < arr.length && low >= 0) { // Check bounds before highlighting for single element case
                visualizer.highlightBlock(low, LESS_THAN_PIVOT_COLOR); // Indicate single element as sorted
            }
            sleep(ANIMATION_DELAY);
            if (low < arr.length && low >= 0) {
                visualizer.resetBlockColor(low); // Reset highlight after base case visualization
            }
        }
    }

    /**
     * Partitions the array: elements less than pivot to the left, greater to the right.
     * Enhanced visualization to show comparisons and swaps.
     *
     * @param arr the array to partition
     * @param low the starting index of the partition
     * @param high the ending index of the partition (pivot is chosen from here)
     * @param stepByStep if true, visualizes each step
     * @param visualizer the VisualizerPanel for display
     * @param task Task for checking cancellation
     * @return the index of the pivot after partitioning
     */
    private static int partition(int[] arr, int low, int high, boolean stepByStep,
                                 VisualizerPanel visualizer, Task<?> task) {
        int pivot = arr[high]; // Choose rightmost element as pivot - common strategy

        if (stepByStep) {
            visualizer.updateStatus("Choosing pivot element: " + pivot + " (at index " + high + ")");
            visualizer.highlightBlock(high, PIVOT_COLOR); // Highlight pivot in pivot color
            sleep(ANIMATION_DELAY);
            if (task.isCancelled()) return low - 1; // Early cancellation check
        }

        int i = low - 1; // Index of smaller element

        for (int j = low; j < high && !task.isCancelled(); j++) { // Iterate through the partition
            if (stepByStep) {
                visualizer.updateStatus("Comparing element " + arr[j] + " (index " + j + ") with pivot " + pivot);
                visualizer.highlightBlock(j, COMPARING_COLOR); // Highlight element being compared
                sleep(ANIMATION_DELAY / 2); // Shorter delay for comparisons
                if (task.isCancelled()) return low - 1;
            }

            if (arr[j] < pivot) { // If current element is smaller than pivot
                i++; // Increment index of smaller element
                if (stepByStep && i != j) { // Avoid swap and visualization if i and j are the same
                    visualizer.updateStatus("Element " + arr[j] + " is smaller than pivot. Swapping with element at index " + i);
                }
                swap(arr, i, j); // Swap arr[i] and arr[j]

                if (stepByStep && i != j) {
                    visualizer.displayArray(arr); // Update display after swap
                    visualizer.animateSwap(i, j); // Animate the swap
                    visualizer.highlightBlock(i, PARTITION_COLOR); // Highlight partition boundary
                    sleep(ANIMATION_DELAY);
                    if (task.isCancelled()) return low - 1;
                } else if (stepByStep && i == j) {
                    visualizer.highlightBlock(i, PARTITION_COLOR); // Still highlight partition boundary if no swap needed
                    sleep(ANIMATION_DELAY/2); // Shorter delay if no swap
                    if (task.isCancelled()) return low - 1;
                }
            } else {
                if (stepByStep) {
                    visualizer.updateStatus("Element " + arr[j] + " is greater than or equal to pivot, no swap.");
                    sleep(ANIMATION_DELAY/2); // Shorter delay for no swap case
                    if (task.isCancelled()) return low - 1;
                }
            }

            if (stepByStep) {
                resetElementColors(visualizer, low, high, i, j); // Reset colors for next iteration
            }
        }

        // Swap pivot (arr[high]) with element at (i+1) - placing pivot in correct sorted position
        swap(arr, i + 1, high);
        if (stepByStep) {
            visualizer.displayArray(arr); // Update display to show pivot placement
            visualizer.updateStatus("Placing pivot " + pivot + " at its final sorted position (index " + (i + 1) + ")");
            visualizer.animateSwap(i + 1, high); // Animate pivot swap
            visualizer.highlightBlock(i + 1, LESS_THAN_PIVOT_COLOR); // Highlight pivot as sorted within partition
            sleep(ANIMATION_DELAY);
        }

        return i + 1; // Return pivot index
    }

    /**
     * Highlights the range of the current partition being processed.
     * Uses a more descriptive name than `highlightPartition`.
     */
    private static void highlightPartitionRange(VisualizerPanel visualizer, int low, int high) {
        for (int i = low; i <= high; i++) {
            visualizer.highlightBlock(i, Color.LIGHTBLUE); // Use light blue to indicate partition range
        }
    }

    /**
     * Resets the highlight of the partition range after subarrays are sorted.
     */
    private static void resetPartitionHighlight(VisualizerPanel visualizer, int low, int high) {
        for (int i = low; i <= high; i++) {
            visualizer.resetBlockColor(i); // Revert to default color after partition is processed
        }
    }


    /**
     * Resets colors of elements after each comparison in partition, keeping pivot and partition boundary highlighted.
     * More targeted color reset for better visualization clarity.
     */
    private static void resetElementColors(VisualizerPanel visualizer, int low, int high, int partitionIndex, int currentElementIndex) {
        for (int k = low; k <= high; k++) {
            if (k == high) {
                visualizer.highlightBlock(k, PIVOT_COLOR); // Keep pivot highlighted
            } else if (k <= partitionIndex) {
                visualizer.highlightBlock(k, PARTITION_COLOR); // Keep partition boundary highlighted
            } else if (k == currentElementIndex) {
                visualizer.resetBlockColor(k); // Reset color of the element just compared if it's not pivot or boundary
            } else if (k > partitionIndex && k < high && k != currentElementIndex) {
                visualizer.highlightBlock(k, DEFAULT_COLOR); // Ensure default color for elements not involved in current step
            }
        }
    }


    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void sleep() {
        sleep(ANIMATION_DELAY);
    }
}