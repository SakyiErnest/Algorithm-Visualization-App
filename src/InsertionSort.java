import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import java.util.Arrays;

public class InsertionSort {
    /**
     * Sorts the array using the Insertion Sort algorithm with enhanced visualization.
     *
     * @param array       the array to sort
     * @param stepByStep  if true, sends animated updates to the visualizer at each insertion and shift
     * @param visualizer  the VisualizerPanel used to display steps
     * @param task        the current Task for cancellation checking and progress updates
     */
    public static void sort(int[] array, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        int n = array.length;

        // Display the initial array state
        if (stepByStep) {
            visualizer.displayArray(array);
            visualizer.updateStatus("🚀 Starting Insertion Sort");
            sleep(300);
        }

        for (int i = 1; i < n; i++) {
            int key = array[i];
            int j = i - 1;

            if (stepByStep) {
                // Highlight the key element being inserted.
                visualizer.highlightBlock(i, Color.ORANGE);
                visualizer.updateStatus("🔑 Key " + key + " selected for insertion");
                sleep(300);
            }

            // Shift elements that are greater than the key to one position ahead.
            while (j >= 0 && array[j] > key) {
                array[j + 1] = array[j]; // Logical shift in the array
                if (stepByStep) {
                    // Animate shifting: move the block at index j to index j+1.
                    visualizer.animateShiftRight(j, j + 1);
                    visualizer.updateStatus("↔️ Shifting " + array[j] + " from index " + j + " to index " + (j + 1));
                    sleep(200);
                    if (task.isCancelled()) {
                        return;
                    }
                }
                j = j - 1;
            }
            array[j + 1] = key; // Insert the key into its correct position

            if (stepByStep) {
                // Refresh the display and update the status message.
                visualizer.displayArray(array);
                visualizer.updateStatus("✅ Inserted " + key + " at position " + (j + 1));
                sleep(300);
                if (task.isCancelled()) {
                    return;
                }
            }
        }
    }

    // Utility method for pausing execution so that animations can be observed.
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
