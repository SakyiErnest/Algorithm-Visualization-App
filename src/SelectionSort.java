import javafx.concurrent.Task;
import javafx.scene.paint.Color;

public class SelectionSort {
    /**
     * Sorts the array using the Selection Sort algorithm.
     *
     * @param arr         the array to sort
     * @param stepByStep  if true, sends updates to the visualizer at each swap
     * @param visualizer  the VisualizerPanel used to display steps
     * @param task        the current Task for cancellation checking and progress updates
     */
    public static void sort(int[] arr, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        // Display the initial array state if step-by-step visualization is enabled
        if (stepByStep) {
            visualizer.displayArray(arr);
            visualizer.updateStatus("Starting Selection Sort");
            sleep(600);
        }

        for (int i = 0; i < arr.length - 1; i++) {
            int minIndex = i;
            // Optionally highlight the current index being examined
            if (stepByStep) {
                visualizer.highlightBlock(i, Color.ORANGE);
                visualizer.updateStatus("Examining index " + i);
                sleep(300);
            }

            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                }
            }

            if (minIndex != i) {
                // Swap the found minimum element with the element at index i
                swap(arr, i, minIndex);
                if (stepByStep) {
                    visualizer.updateStatus("Swapping index " + i + " and index " + minIndex);
                    visualizer.animateSwap(i, minIndex);
                    sleep(600); // Pause so the user can see the animation
                    if (task.isCancelled()) {
                        return;
                    }
                }
            }
        }
    }

    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // Utility method to pause the execution so animations can be observed.
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
