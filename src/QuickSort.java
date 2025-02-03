import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import java.util.Arrays;


public class QuickSort {
    // Constants for visualization
    private static final Color PIVOT_COLOR = Color.ORANGE;
    private static final Color CURRENT_COMPARISON_COLOR = Color.YELLOW;
    private static final Color SORTED_COLOR = Color.LIGHTGREEN;
    private static final Color PARTITION_BOUNDARY_COLOR = Color.PURPLE;
    private static final int ANIMATION_DELAY = 600;

    /**
     * Main sorting method with enhanced visualization
     */
    public static void sort(int[] arr, int low, int high, boolean stepByStep,
                            VisualizerPanel visualizer, Task<?> task) {
        if (stepByStep) {
            visualizer.updateStatus("Starting QuickSort on subarray [" + low + ".." + high + "]");
            highlightPartition(visualizer, arr, low, high);
            sleep(ANIMATION_DELAY);
        }

        if (low < high && !task.isCancelled()) {
            // Select and highlight pivot
            int pivotIndex = partition(arr, low, high, stepByStep, visualizer, task);

            if (stepByStep) {
                visualizer.updateStatus("Partition complete. Pivot " + arr[pivotIndex] +
                        " is now in its final position");
                visualizer.highlightBlock(pivotIndex, SORTED_COLOR);
                sleep(ANIMATION_DELAY);
            }

            // Recursively sort sub-arrays
            sort(arr, low, pivotIndex - 1, stepByStep, visualizer, task);
            sort(arr, pivotIndex + 1, high, stepByStep, visualizer, task);

            if (stepByStep) {
                markSortedRange(visualizer, low, high);
            }
        }
    }

    /**
     * Enhanced partition method with detailed visualization
     */
    private static int partition(int[] arr, int low, int high, boolean stepByStep,
                                 VisualizerPanel visualizer, Task<?> task) {
        if (stepByStep) {
            visualizer.updateStatus("Selecting pivot: " + arr[high]);
            visualizer.highlightBlock(high, PIVOT_COLOR);
            sleep(ANIMATION_DELAY);
        }

        int pivot = arr[high];
        int i = low - 1;

        for (int j = low; j < high && !task.isCancelled(); j++) {
            if (stepByStep) {
                visualizer.updateStatus("Comparing element " + arr[j] + " with pivot " + pivot);
                visualizer.highlightBlock(j, CURRENT_COMPARISON_COLOR);
                sleep(ANIMATION_DELAY/2);
            }

            if (arr[j] < pivot) {
                i++;
                if (stepByStep) {
                    visualizer.updateStatus("Element " + arr[j] + " is less than pivot. " +
                            "Swapping with element at position " + i);
                }

                swap(arr, i, j);

                if (stepByStep) {
                    visualizer.displayArray(arr);
                    visualizer.animateSwap(i, j);
                    visualizer.highlightBlock(i, PARTITION_BOUNDARY_COLOR);
                    sleep(ANIMATION_DELAY);
                }
            }

            if (stepByStep) {
                resetColors(visualizer, arr, low, high, i);
            }
        }

        // Place pivot in its final position
        swap(arr, i + 1, high);

        if (stepByStep) {
            visualizer.displayArray(arr);
            visualizer.updateStatus("Placing pivot in its final position");
            visualizer.animateSwap(i + 1, high);
            visualizer.highlightBlock(i + 1, SORTED_COLOR);
            sleep(ANIMATION_DELAY);
        }

        return i + 1;
    }

    /**
     * Highlights the current partition being processed
     */
    private static void highlightPartition(VisualizerPanel visualizer, int[] arr,
                                           int low, int high) {
        for (int i = 0; i < arr.length; i++) {
            if (i >= low && i <= high) {
                visualizer.highlightBlock(i, Color.LIGHTBLUE);
            } else {
                visualizer.highlightBlock(i, Color.GRAY);
            }
        }
    }

    /**
     * Marks a range as sorted
     */
    private static void markSortedRange(VisualizerPanel visualizer, int low, int high) {
        for (int i = low; i <= high; i++) {
            visualizer.highlightBlock(i, SORTED_COLOR);
        }
        sleep(ANIMATION_DELAY/2);
    }

    /**
     * Resets colors during partition operation
     */
    private static void resetColors(VisualizerPanel visualizer, int[] arr,
                                    int low, int high, int partitionIndex) {
        for (int k = low; k <= high; k++) {
            if (k == high) {
                visualizer.highlightBlock(k, PIVOT_COLOR);
            } else if (k <= partitionIndex) {
                visualizer.highlightBlock(k, PARTITION_BOUNDARY_COLOR);
            } else {
                visualizer.highlightBlock(k, Color.LIGHTBLUE);
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
}