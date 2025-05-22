import javafx.concurrent.Task;
import javafx.scene.paint.Color;

/**
 * Implementation of the Selection Sort algorithm with enhanced visualization.
 */
public class SelectionSort {
    // Colors for different states in the visualization
    private static final Color CURRENT_INDEX_COLOR = Color.web("#f97316");    // Orange
    private static final Color COMPARING_COLOR = Color.web("#fbbf24");        // Yellow
    private static final Color MIN_INDEX_COLOR = Color.web("#ec4899");        // Pink
    private static final Color SORTED_COLOR = Color.web("#34d399");           // Green

    /**
     * Sorts the array using the Selection Sort algorithm.
     *
     * @param arr         the array to sort
     * @param stepByStep  if true, sends updates to the visualizer at each step
     * @param visualizer  the VisualizerPanel used to display steps
     * @param task        the current Task for cancellation checking and progress updates
     */
    public static void sort(int[] arr, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        // Display the initial array state
        if (stepByStep) {
            visualizer.displayArray(arr);
            visualizer.updateStatus("Starting Selection Sort");
            visualizer.highlightPseudocodeLine(0); // Highlight the algorithm title
            sleep(800);
        }

        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            if (task.isCancelled()) return;

            // Highlight current position in the outer loop
            if (stepByStep) {
                visualizer.updateStatus("Outer loop: i = " + i + ", finding minimum element");
                visualizer.highlightPseudocodeLine(1); // for i = 0 to n-2
                visualizer.highlightBlock(i, CURRENT_INDEX_COLOR);
                sleep(500);
            }

            // Find the minimum element in the unsorted part
            int minIndex = i;
            if (stepByStep) {
                visualizer.updateStatus("Initially assuming minimum is at index " + i);
                visualizer.highlightPseudocodeLine(2); // minIndex = i
                visualizer.highlightBlock(minIndex, MIN_INDEX_COLOR);
                sleep(500);
            }

            // Iterate through unsorted elements to find minimum
            for (int j = i + 1; j < n; j++) {
                if (task.isCancelled()) return;

                if (stepByStep) {
                    visualizer.updateStatus("Comparing: arr[" + j + "] = " + arr[j] +
                            " with current minimum arr[" + minIndex + "] = " + arr[minIndex]);
                    visualizer.highlightPseudocodeLine(3); // for j = i+1 to n-1
                    visualizer.highlightBlock(j, COMPARING_COLOR);
                    sleep(300); // Brief pause for comparison
                }

                if (arr[j] < arr[minIndex]) {
                    // Found a new minimum
                    if (stepByStep) {
                        visualizer.updateStatus("New minimum found at index " + j);
                        visualizer.highlightPseudocodeLine(4); // if arr[j] < arr[minIndex]

                        // Reset previous minimum color
                        visualizer.resetBlockColor(minIndex);
                        sleep(100);
                    }

                    minIndex = j;

                    if (stepByStep) {
                        visualizer.highlightPseudocodeLine(5); // minIndex = j
                        visualizer.highlightBlock(minIndex, MIN_INDEX_COLOR);
                        sleep(300);
                    }
                } else {
                    if (stepByStep) {
                        // Reset comparison highlight
                        visualizer.resetBlockColor(j);
                        sleep(100);
                    }
                }
            }

            // Swap the found minimum element with the first element of unsorted part
            if (stepByStep) {
                visualizer.updateStatus("Swapping element at index " + i +
                        " with minimum element at index " + minIndex);
                visualizer.highlightPseudocodeLine(6); // swap(arr[i], arr[minIndex])
                sleep(400);
            }

            // Perform the swap (even if i == minIndex, for visualization consistency)
            swap(arr, i, minIndex);

            if (stepByStep) {
                visualizer.animateSwap(i, minIndex);
                sleep(600);

                // Mark the element at position i as sorted
                visualizer.highlightBlock(i, SORTED_COLOR);
                visualizer.updateStatus("Element at index " + i + " is now in its final sorted position");
                sleep(400);
            }

            // Report progress (using a different approach)
            if (task != null && n > 1) {
                // Calculate progress as a percentage (0.0 to 1.0)
                double progress = (double)(i + 1) / (n - 1);
                reportProgress(task, progress);
            }
        }

        // Mark the last element as sorted (it's automatically in the correct position)
        if (stepByStep && n > 0) {
            visualizer.highlightBlock(n - 1, SORTED_COLOR);
            visualizer.updateStatus("Selection Sort complete! Array is now sorted.");
            sleep(500);

            // Final display to show the fully sorted array
            for (int i = 0; i < n; i++) {
                visualizer.highlightBlock(i, SORTED_COLOR);
                sleep(50);
            }
        }
    }

    /**
     * Swaps two elements in the array.
     */
    private static void swap(int[] arr, int i, int j) {
        if (i != j) {  // Only swap if indices are different
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    /**
     * Helper method to report progress back to the task using reflection
     * to access the protected updateProgress method.
     */
    private static void reportProgress(Task<?> task, double progress) {
        try {
            // This is a workaround since we can't directly access the protected updateProgress method
            java.lang.reflect.Method method = Task.class.getDeclaredMethod("updateProgress", double.class, double.class);
            method.setAccessible(true);
            method.invoke(task, progress, 1.0);
        } catch (Exception e) {
            // Silently ignore if we can't update progress
            System.err.println("Could not update progress: " + e.getMessage());
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
}