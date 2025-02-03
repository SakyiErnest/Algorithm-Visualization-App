import javafx.concurrent.Task;
import java.util.Arrays;

public class RadixSort {

    // Adjust these as needed
    private static final int ANIMATION_DELAY = 300;

    /**
     * Sorts the array using the Radix Sort algorithm with improved visualization feedback.
     *
     * @param arr         the array to sort
     * @param stepByStep  if true, sends animated updates to the visualizer after sorting each digit
     * @param visualizer  the VisualizerPanel used to display steps and animations
     * @param task        the current Task for cancellation checking and progress updates
     */
    public static void sort(int[] arr, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        int max = getMax(arr);

        if (stepByStep) {
            visualizer.displayArray(arr);
            visualizer.updateStatus("Starting Radix Sort. Maximum value: " + max);
            sleep(ANIMATION_DELAY);
        }

        // Process each digit from least significant digit ( LSD ) to most significant
        for (int exp = 1; max / exp > 0; exp *= 10) {
            // If stepByStep is true, show detailed feedback
            if (stepByStep) {
                visualizer.updateStatus(
                        String.format("Sorting by digit (exp = %d). Elements: %s", exp, Arrays.toString(arr))
                );
            }
            // Sort by the current digit
            countSortByDigit(arr, exp, stepByStep, visualizer, task);

            if (task.isCancelled()) {
                return;
            }

            // Show updated array and status
            if (stepByStep) {
                visualizer.displayArray(arr);
                visualizer.updateStatus("Completed pass for exp = " + exp + ".");
                sleep(ANIMATION_DELAY);
            }
        }

        // Final update
        if (stepByStep) {
            visualizer.updateStatus("Radix Sort completed. Final array: " + Arrays.toString(arr));
        }
    }

    /**
     * Returns the maximum value in the array.
     */
    private static int getMax(int[] arr) {
        int mx = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > mx) {
                mx = arr[i];
            }
        }
        return mx;
    }

    /**
     * Performs a digit-based sorting pass (similar to Counting Sort for a specific digit).
     */
    private static void countSortByDigit(int[] arr, int exp, boolean stepByStep,
                                         VisualizerPanel visualizer, Task<?> task) {
        int n = arr.length;
        int[] output = new int[n];
        int[] count = new int[10];

        // Count occurrences of each digit in this position
        for (int value : arr) {
            int digit = (value / exp) % 10;
            count[digit]++;
        }

        // Compute cumulative count so that count[i] contains positions up to digit i
        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];
        }

        // Build the output array from the end to maintain stable sorting
        for (int i = n - 1; i >= 0; i--) {
            if (task.isCancelled()) {
                return;
            }
            int digit = (arr[i] / exp) % 10;
            output[count[digit] - 1] = arr[i];
            count[digit]--;

            // Optional: highlight the digit currently being placed
            if (stepByStep) {
                visualizer.updateStatus("Placing element " + arr[i] + " into position " + (count[digit] + 1));
                // Highlight the block (i) in a unique color if your Visualizer supports it
                visualizer.displayArray(output);
                sleep(ANIMATION_DELAY / 2);
            }
        }

        // Copy output back to arr
        for (int i = 0; i < n; i++) {
            arr[i] = output[i];
        }
    }

    /**
     * Utility method to pause execution so that animations can be observed
     */
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}