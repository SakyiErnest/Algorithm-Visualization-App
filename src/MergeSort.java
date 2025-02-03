import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import java.util.Arrays;

public class MergeSort {
    /**
     * Sorts the array using the Merge Sort algorithm.
     *
     * @param arr the array to sort
     * @param l the starting index of the subarray to sort
     * @param r the ending index of the subarray to sort
     * @param stepByStep if true, sends updates to the visualizer for each merge operation
     * @param visualizer the VisualizerPanel used to display steps
     * @param task the current Task for cancellation checking and progress updates
     */
    public static void sort(int arr[], int l, int r, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        if (l < r) {
            int m = (l + r) / 2;
            sort(arr, l, m, stepByStep, visualizer, task);
            sort(arr, m + 1, r, stepByStep, visualizer, task);
            merge(arr, l, m, r, stepByStep, visualizer, task);
        }
    }

    /**
     * Merges two sorted subarrays of arr.
     * The first subarray is arr[l..m] and the second subarray is arr[m+1..r].
     */
    public static void merge(int arr[], int l, int m, int r, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        int n1 = m - l + 1;
        int n2 = r - m;

        int L[] = new int[n1];
        int R[] = new int[n2];

        // Copy data to temporary arrays
        for (int i = 0; i < n1; ++i)
            L[i] = arr[l + i];
        for (int j = 0; j < n2; ++j)
            R[j] = arr[m + 1 + j];

        int i = 0, j = 0;
        int k = l;
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }

        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }

        if (stepByStep) {
            // Update the display to show the current array state
            visualizer.displayArray(arr);
            // Update the status message to indicate which subarray was merged
            visualizer.updateStatus("Merging subarray [" + l + ", " + r + "]: " + Arrays.toString(Arrays.copyOfRange(arr, l, r + 1)));

            // Optionally, highlight the merged portion with a temporary color
            for (int index = l; index <= r; index++) {
                visualizer.highlightBlock(index, Color.LIGHTGREEN);
            }
            sleep(600);  // Pause so the user can see the updated and highlighted subarray
            if (task.isCancelled()) {
                return;
            }
        }
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
