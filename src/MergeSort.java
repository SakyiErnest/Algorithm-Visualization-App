import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Arrays;

/**
 * Enhanced MergeSort implementation with advanced visualizations designed for learning.
 */
public class MergeSort {
    // Enhanced color palette for better visual distinction
    private static final Color DIVIDING_COLOR = Color.web("#805ad5");      // Purple for division phase
    private static final Color LEFT_SUBARRAY_COLOR = Color.web("#3182ce"); // Blue for left subarrays
    private static final Color RIGHT_SUBARRAY_COLOR = Color.web("#e53e3e"); // Red for right subarrays
    private static final Color COMPARING_COLOR = Color.web("#ecc94b");     // Yellow for comparisons
    private static final Color MERGED_COLOR = Color.web("#38a169");        // Green for merged elements
    private static final Color DEFAULT_COLOR = VisualizerPanel.DEFAULT_BLOCK_COLOR;

    // Track recursion depth for visualization
    private static int maxRecursionDepth = 0;
    private static int totalOperations = 0;
    private static int completedOperations = 0;

    /**
     * Main entry point for sorting - keeping the original method signature
     * to match AlgorithmExecutor's call pattern
     */
    public static void sort(int[] arr, int left, int right, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        // Initialize tracking variables
        maxRecursionDepth = 0;
        totalOperations = arr.length > 0 ? (int)(2 * arr.length * (Math.log(arr.length) / Math.log(2))) : 0;
        completedOperations = 0;

        if (stepByStep) {
            visualizer.updateStatus("Starting Merge Sort - A divide and conquer algorithm");
            visualizer.highlightPseudocodeLine(0); // Title
            sleep(1000);

            // Initial explanation for learners
            showExplanation(visualizer,
                    "Merge Sort works by dividing the array into halves recursively " +
                            "until we have single elements, then merging them back in sorted order.", 2000);
        }

        // Call the recursive merge sort
        mergeSort(arr, left, right, stepByStep, visualizer, task, 0);

        // Final visualization
        if (stepByStep && !task.isCancelled()) {
            visualizer.displayArray(arr);
            visualizer.updateStatus("Merge Sort complete! Array is now sorted: " + Arrays.toString(arr));

            // Final highlight of the sorted array
            for (int i = 0; i < arr.length; i++) {
                final int index = i;
                Platform.runLater(() -> visualizer.highlightBlock(index, MERGED_COLOR));
                sleep(100);
            }

            showExplanation(visualizer,
                    "Merge Sort is complete! This algorithm has O(n log n) time complexity " +
                            "and is stable, but requires O(n) extra space.", 3000);
        }
    }

    /**
     * Recursive merge sort implementation with enhanced visualization.
     */
    private static void mergeSort(int[] arr, int left, int right, boolean stepByStep,
                                  VisualizerPanel visualizer, Task<?> task, int depth) {
        // Update max recursion depth
        maxRecursionDepth = Math.max(maxRecursionDepth, depth);

        // Base case: If the subarray has 1 or 0 elements, it's already sorted
        if (left >= right) {
            if (stepByStep) {
                visualizer.updateStatus("Subarray [" + left + "] has only one element, already sorted");
                visualizer.highlightPseudocodeLine(2); // Base case in pseudocode
                visualizer.highlightBlock(left, MERGED_COLOR);
                sleep(500);
                visualizer.resetBlockColor(left);
            }
            return;
        }

        // Calculate middle point
        int mid = left + (right - left) / 2;

        if (stepByStep) {
            visualizer.highlightPseudocodeLine(3); // mid = (left + right) / 2
            visualizer.updateStatus("Dividing array at index " + mid + " (recursion depth: " + depth + ")");

            // Visualize division
            visualizeDivision(visualizer, arr, left, mid, right, depth);

            if (task.isCancelled()) return;
        }

        // Recursive calls - left half
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(4); // mergeSort(arr, left, mid)
            visualizer.updateStatus("Sorting left half [" + left + " to " + mid + "]");
            highlightSubarray(visualizer, left, mid, LEFT_SUBARRAY_COLOR);
            sleep(500);
        }

        mergeSort(arr, left, mid, stepByStep, visualizer, task, depth + 1);

        if (task.isCancelled()) return;

        // Recursive calls - right half
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(5); // mergeSort(arr, mid+1, right)
            visualizer.updateStatus("Sorting right half [" + (mid + 1) + " to " + right + "]");
            highlightSubarray(visualizer, mid + 1, right, RIGHT_SUBARRAY_COLOR);
            sleep(500);
        }

        mergeSort(arr, mid + 1, right, stepByStep, visualizer, task, depth + 1);

        if (task.isCancelled()) return;

        // Merge the sorted halves
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(6); // merge(arr, left, mid, right)
            visualizer.updateStatus("Merging sorted subarrays [" + left + "-" + mid + "] and [" + (mid+1) + "-" + right + "]");
        }

        merge(arr, left, mid, right, stepByStep, visualizer, task);

        // Update progress after each merge operation
        updateProgress(task, arr.length);
    }

    /**
     * Enhanced merge operation with improved visualization of the merging process.
     */
    private static void merge(int[] arr, int left, int mid, int right, boolean stepByStep,
                              VisualizerPanel visualizer, Task<?> task) {
        // Create temporary arrays
        int n1 = mid - left + 1; // Size of left subarray
        int n2 = right - mid;    // Size of right subarray

        int[] L = new int[n1];
        int[] R = new int[n2];

        // Copy data to temp arrays
        for (int i = 0; i < n1; i++)
            L[i] = arr[left + i];
        for (int j = 0; j < n2; j++)
            R[j] = arr[mid + 1 + j];

        if (stepByStep) {
            visualizer.highlightPseudocodeLine(9); // Create temporary arrays
            visualizer.updateStatus("Creating temporary arrays for merging");

            // Visualize the temporary arrays
            visualizeTempArrays(visualizer, L, R, left, mid, right);
            sleep(800);

            if (task.isCancelled()) return;
        }

        // Merge the temp arrays
        int i = 0, j = 0; // Initial indices of left and right subarrays
        int k = left;     // Initial index of merged subarray

        // Compare elements from both arrays and merge
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(14); // Initial indices for merging
            visualizer.updateStatus("Starting to merge elements by comparing them");
            sleep(500);
        }

        while (i < n1 && j < n2) {
            if (stepByStep) {
                visualizer.highlightPseudocodeLine(15); // While both arrays have elements

                // Visualize comparison between two elements
                visualizeComparison(visualizer, L[i], R[j], left + i, mid + 1 + j);

                if (task.isCancelled()) return;
            }

            if (L[i] <= R[j]) {
                // Left element is smaller
                if (stepByStep) {
                    visualizer.highlightPseudocodeLine(16); // if L[i] <= R[j]
                    visualizer.updateStatus("Left element " + L[i] + " ≤ Right element " + R[j] + ": Selecting from left");
                    visualizeMergeSelection(visualizer, L[i], k, true);
                    sleep(600);
                }

                arr[k] = L[i];
                i++;
            } else {
                // Right element is smaller
                if (stepByStep) {
                    visualizer.highlightPseudocodeLine(19); // else
                    visualizer.updateStatus("Left element " + L[i] + " > Right element " + R[j] + ": Selecting from right");
                    visualizeMergeSelection(visualizer, R[j], k, false);
                    sleep(600);
                }

                arr[k] = R[j];
                j++;
            }

            // Update display after each element is placed
            if (stepByStep) {
                visualizer.displayArray(arr);
                visualizer.highlightBlock(k, MERGED_COLOR);
                sleep(300);
            }

            k++;
        }

        // Copy remaining elements of L[] if any
        while (i < n1) {
            if (stepByStep) {
                visualizer.highlightPseudocodeLine(23); // Copy remaining elements of L
                visualizer.updateStatus("Copying remaining element " + L[i] + " from left array");
                visualizeMergeSelection(visualizer, L[i], k, true);
                sleep(300);
            }

            arr[k] = L[i];
            i++;
            k++;

            if (stepByStep) {
                visualizer.displayArray(arr);
                visualizer.highlightBlock(k - 1, MERGED_COLOR);
                sleep(200);
            }
        }

        // Copy remaining elements of R[] if any
        while (j < n2) {
            if (stepByStep) {
                visualizer.highlightPseudocodeLine(24); // Copy remaining elements of R
                visualizer.updateStatus("Copying remaining element " + R[j] + " from right array");
                visualizeMergeSelection(visualizer, R[j], k, false);
                sleep(300);
            }

            arr[k] = R[j];
            j++;
            k++;

            if (stepByStep) {
                visualizer.displayArray(arr);
                visualizer.highlightBlock(k - 1, MERGED_COLOR);
                sleep(200);
            }
        }

        // Visualize the completed merge
        if (stepByStep) {
            visualizer.updateStatus("Merged subarray [" + left + "-" + right + "] " +
                    "(depth " + (maxRecursionDepth - Math.min(left, right)) + "): " +
                    Arrays.toString(Arrays.copyOfRange(arr, left, right + 1)));

            // Highlight the merged subarray
            highlightSubarray(visualizer, left, right, MERGED_COLOR);
            sleep(Math.max(300, 800 - (Math.min(left, right) * 50))); // Shorter pause for deeper recursion levels

            // Reduce highlight intensity for completed merges
            Color fadedColor = MERGED_COLOR.deriveColor(0, 1, 1, 0.7);
            highlightSubarray(visualizer, left, right, fadedColor);
        }
    }

    /**
     * Visualizes the division of an array into two halves.
     */
    private static void visualizeDivision(VisualizerPanel visualizer, int[] arr, int left, int mid, int right, int depth) {
        // Show division point with animation
        Platform.runLater(() -> {
            // Create a dividing line effect
            for (int i = left; i <= right; i++) {
                visualizer.highlightBlock(i, DIVIDING_COLOR);
                sleep(50);
            }

            // Then show the two halves
            for (int i = left; i <= mid; i++) {
                visualizer.highlightBlock(i, LEFT_SUBARRAY_COLOR);
            }
            for (int i = mid + 1; i <= right; i++) {
                visualizer.highlightBlock(i, RIGHT_SUBARRAY_COLOR);
            }
        });

        // Educational message about the divide step
        String message = String.format("Divide step: Array[%d-%d] split into Array[%d-%d] and Array[%d-%d]",
                left, right, left, mid, mid+1, right);
        visualizer.updateStatus(message);

        sleep(700);
    }

    /**
     * Visualizes the temporary arrays used in the merge process.
     */
    private static void visualizeTempArrays(VisualizerPanel visualizer, int[] L, int[] R,
                                            int left, int mid, int right) {
        Platform.runLater(() -> {
            // First highlight the subarrays being copied
            for (int i = left; i <= mid; i++) {
                visualizer.highlightBlock(i, LEFT_SUBARRAY_COLOR);
            }
            for (int i = mid + 1; i <= right; i++) {
                visualizer.highlightBlock(i, RIGHT_SUBARRAY_COLOR);
            }

            // Create visual representation of temp arrays
            String leftArrayStr = "L[] = " + Arrays.toString(L);
            String rightArrayStr = "R[] = " + Arrays.toString(R);

            visualizer.updateStatus("Created temporary arrays: " + leftArrayStr + ", " + rightArrayStr);
        });

        sleep(800);
    }

    /**
     * Visualizes the comparison of two elements during merging.
     */
    private static void visualizeComparison(VisualizerPanel visualizer, int leftVal, int rightVal,
                                            int leftIdx, int rightIdx) {
        Platform.runLater(() -> {
            visualizer.highlightBlock(leftIdx, COMPARING_COLOR);
            visualizer.highlightBlock(rightIdx, COMPARING_COLOR);
        });

        String comparisonStr = leftVal + " vs " + rightVal + ": ";
        comparisonStr += (leftVal <= rightVal) ? "Left is smaller or equal" : "Right is smaller";
        visualizer.updateStatus("Comparing: " + comparisonStr);

        sleep(500);
    }

    /**
     * Visualizes the selection of an element during merging.
     * Simplified version that doesn't require source index
     */
    private static void visualizeMergeSelection(VisualizerPanel visualizer, int value, int targetIdx, boolean isFromLeft) {
        Color selectionColor = isFromLeft ? LEFT_SUBARRAY_COLOR : RIGHT_SUBARRAY_COLOR;

        Platform.runLater(() -> {
            visualizer.highlightBlock(targetIdx, selectionColor);
        });

        String source = isFromLeft ? "left" : "right";
        visualizer.updateStatus("Selected " + value + " from " + source + " subarray for position " + targetIdx);

        sleep(300);
    }

    /**
     * Highlights a subarray with the specified color.
     */
    private static void highlightSubarray(VisualizerPanel visualizer, int start, int end, Color color) {
        Platform.runLater(() -> {
            for (int i = start; i <= end; i++) {
                visualizer.highlightBlock(i, color);
            }
        });
    }

    /**
     * Shows an educational explanation during the sorting process.
     */
    private static void showExplanation(VisualizerPanel visualizer, String text, int duration) {
        visualizer.updateStatus("ℹ️ " + text);
        sleep(duration);
    }

    /**
     * Updates the progress in the task.
     */
    private static void updateProgress(Task<?> task, int arrayLength) {
        completedOperations++;
        if (task != null && totalOperations > 0) {
            try {
                // Use reflection to access protected method
                java.lang.reflect.Method method = Task.class.getDeclaredMethod(
                        "updateProgress", double.class, double.class);
                method.setAccessible(true);
                method.invoke(task, (double)completedOperations, (double)totalOperations);
            } catch (Exception e) {
                // Silently ignore if progress can't be updated
            }
        }
    }

    /**
     * Utility method to pause the execution so animations can be observed.
     */
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Default sleep duration.
     */
    private static void sleep() {
        sleep(600); // Default sleep duration
    }
}