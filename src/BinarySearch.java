import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import java.util.Arrays;

public class BinarySearch {
    /**
     * Searches for an element in a (sorted) array using the Binary Search algorithm.
     * If the array is not sorted, it will be sorted before searching.
     * The method returns the index of any matching element if there are duplicates.
     *
     * @param array            the array in which to search
     * @param elementToSearch  the element to search for
     * @param stepByStep       if true, sends animated updates to the visualizer for each step
     * @param visualizer       the VisualizerPanel used to display steps
     * @param task             the current Task for cancellation checking and progress updates
     * @return the index of the element if found, or -1 if not found
     */
    public static int search(int[] array, int elementToSearch, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        // Option to visualize sorting (if not already sorted)
        boolean visualizeSorting = false; // You could make this a parameter or UI option

        // Ensure the array is sorted before performing binary search.
        if (!isSorted(array)) {
            if (visualizeSorting) {
                // Visualize the sorting process (using Merge Sort in this example)
                MergeSort.sort(array, 0, array.length - 1, stepByStep, visualizer, task);
            } else {
                Arrays.sort(array);
            }

            if (stepByStep) {
                visualizer.displayArray(array);
                visualizer.updateStatus("Array unsorted. Sorted array: " + Arrays.toString(array));
                sleep(300);
                if (task.isCancelled()) {
                    return -1;
                }
            }
        } else if (stepByStep) {
            visualizer.displayArray(array);
            visualizer.updateStatus("Array is sorted. Starting Binary Search.");
            sleep(300);
        }

        int firstIndex = 0;
        int lastIndex = array.length - 1;

        while (firstIndex <= lastIndex) {
            int middleIndex = (firstIndex + lastIndex) / 2;
            if (stepByStep) {
                // Highlight first, middle, and last elements.
                visualizer.highlightBlock(firstIndex, Color.ORANGE); // Example color
                visualizer.highlightBlock(middleIndex, Color.YELLOW);
                visualizer.highlightBlock(lastIndex, Color.PURPLE); // Example color

                visualizer.updateStatus("Checking index " + middleIndex + " (value: " + array[middleIndex] + ")");
                sleep(200);

                if (task.isCancelled()) {
                    return -1;
                }
                // Reset the block to the default color.
                visualizer.highlightBlock(firstIndex, Color.LIGHTBLUE);
                visualizer.highlightBlock(middleIndex, Color.LIGHTBLUE);
                visualizer.highlightBlock(lastIndex, Color.LIGHTBLUE);
            }

            if (array[middleIndex] == elementToSearch) {
                if (stepByStep) {
                    visualizer.highlightBlock(middleIndex, Color.LIMEGREEN);
                    visualizer.updateStatus("Found " + elementToSearch + " at index " + middleIndex);
                    sleep(300);
                }
                return middleIndex;
            } else if (array[middleIndex] < elementToSearch) {
                // Search in the right half
                if (stepByStep) {
                    visualizer.updateStatus("Target (" + elementToSearch + ") is potentially in the right half.");
                }
                firstIndex = middleIndex + 1;
                // Optionally dim or gray out the left half (elements from 0 to middleIndex)
            } else {
                // Search in the left half
                if (stepByStep) {
                    visualizer.updateStatus("Target (" + elementToSearch + ") is potentially in the left half.");
                }
                lastIndex = middleIndex - 1;
                // Optionally dim or gray out the right half (elements from middleIndex to array.length - 1)
            }
        }

        if (stepByStep) {
            visualizer.updateStatus("Element " + elementToSearch + " not found.");
        }
        return -1; // Element not found
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

    // Utility method to pause execution for animations.
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}