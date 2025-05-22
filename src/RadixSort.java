import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced educational implementation of Radix Sort algorithm.
 * Provides rich visualization and explanation of the digit-by-digit sorting process.
 */
public class RadixSort {
    // Enhanced color scheme for better visual understanding
    private static final Color CURRENT_DIGIT_COLOR = Color.web("#f59e0b");     // Amber for current digit position
    private static final Color PROCESSED_DIGIT_COLOR = Color.web("#10b981");   // Green for processed digits
    private static final Color BUCKET_COLOR = Color.web("#3b82f6");            // Blue for bucket elements
    private static final Color COUNTING_COLOR = Color.web("#8b5cf6");          // Purple for counting array
    private static final Color CURRENT_ELEMENT_COLOR = Color.web("#ec4899");   // Pink for current element

    // Animation timing constants
    private static final int STANDARD_DELAY = 800;
    private static final int FAST_DELAY = 400;
    private static final int VERY_FAST_DELAY = 200;

    /**
     * Sorts the array using the Radix Sort algorithm with enhanced visualization feedback.
     *
     * @param arr         the array to sort
     * @param stepByStep  if true, sends animated updates to the visualizer
     * @param visualizer  the VisualizerPanel used to display steps and animations
     * @param task        the current Task for cancellation checking and progress updates
     */
    public static void sort(int[] arr, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        if (arr.length <= 1) {
            if (stepByStep) {
                visualizer.displayArray(arr);
                visualizer.updateStatus("Array has 0 or 1 elements, already sorted.");
            }
            return;
        }

        // Find the maximum number to know number of digits
        int max = getMax(arr);

        // Calculate the number of digits in max number to track progress
        int maxDigits = (int) Math.log10(max) + 1;
        int totalPasses = maxDigits;
        int currentPass = 0;

        if (stepByStep) {
            visualizer.displayArray(arr);
            visualizer.highlightPseudocodeLine(0); // Title or algorithm name
            visualizer.updateStatus("🚀 Starting Radix Sort - LSD (Least Significant Digit) implementation");
            sleep(STANDARD_DELAY);

            // Educational introduction
            visualizer.updateStatus("📚 Radix Sort works by sorting elements digit-by-digit, starting from the least significant digit");
            sleep(STANDARD_DELAY);

            visualizer.updateStatus("🔢 Maximum value: " + max + " has " + maxDigits + " digits");
            sleep(STANDARD_DELAY);
        }

        // Process each digit from least significant (rightmost) to most significant (leftmost)
        for (int exp = 1; max / exp > 0; exp *= 10) {
            currentPass++;

            if (stepByStep) {
                // Show which digit position we're processing
                int digitPosition = (int) Math.log10(exp) + 1;
                visualizer.highlightPseudocodeLine(1); // For loop for each digit

                String digitMsg = String.format("🔍 Pass %d of %d: Sorting by %s digit (10^%d's place)",
                        currentPass, totalPasses,
                        getOrdinalNumber(digitPosition), digitPosition - 1);
                visualizer.updateStatus(digitMsg);
                sleep(STANDARD_DELAY);

                // Highlight the current digit in each number
                highlightDigitPosition(arr, exp, visualizer);
                sleep(STANDARD_DELAY);
            }

            // Perform counting sort for this digit
            countSortByDigit(arr, exp, stepByStep, visualizer, task, currentPass, totalPasses);

            if (task.isCancelled()) {
                return;
            }

            // Update progress
            if (task != null) {
                updateProgress(task, currentPass, totalPasses);
            }

            // Show updated array after this pass
            if (stepByStep) {
                visualizer.displayArray(arr);
                String passCompleteMsg = String.format("✅ Pass %d complete: Array sorted by first %d digit(s)",
                        currentPass, currentPass);
                visualizer.updateStatus(passCompleteMsg);
                sleep(STANDARD_DELAY);

                // Educational message about stability
                if (currentPass == 1) {
                    visualizer.updateStatus("💡 Radix Sort is stable: elements with same digits maintain their relative order");
                    sleep(STANDARD_DELAY);
                } else if (currentPass == totalPasses / 2) {
                    visualizer.updateStatus("💡 Each pass uses counting sort with time complexity O(n+k) where k is the range of digits (10)");
                    sleep(STANDARD_DELAY);
                }
            }
        }

        // Final update
        if (stepByStep) {
            visualizer.displayArray(arr);
            visualizer.updateStatus("✨ Radix Sort completed successfully!");

            // Highlight all elements as sorted
            for (int i = 0; i < arr.length; i++) {
                visualizer.highlightBlock(i, PROCESSED_DIGIT_COLOR);
                sleep(50); // Quick highlight animation
            }

            sleep(STANDARD_DELAY / 2);

            // Final educational message
            String complexityMsg = "📊 Time Complexity: O(d*(n+k)) where d is the number of digits, n is array size, k is digit range (10)";
            visualizer.updateStatus(complexityMsg);
            sleep(STANDARD_DELAY);

            visualizer.updateStatus("💾 Space Complexity: O(n+k) for auxiliary arrays");
            sleep(STANDARD_DELAY);
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
     * Performs an enhanced counting sort by digit with detailed visualization.
     */
    private static void countSortByDigit(int[] arr, int exp, boolean stepByStep,
                                         VisualizerPanel visualizer, Task<?> task,
                                         int currentPass, int totalPasses) {
        int n = arr.length;
        int[] output = new int[n];
        int[] count = new int[10]; // 10 possible digits (0-9)

        if (stepByStep) {
            visualizer.highlightPseudocodeLine(2); // Initialize count array
            visualizer.updateStatus("📊 Step 1: Initialize counting array for digits 0-9");
            sleep(FAST_DELAY);
        }

        // Step 1: Count occurrences of each digit
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(3); // Count digit occurrences
            visualizer.updateStatus("🔢 Step 2: Count occurrences of each digit at position " + (int)Math.log10(exp) + 1);
            sleep(FAST_DELAY);
        }

        // Create buckets for visualization
        @SuppressWarnings("unchecked")
        List<Integer>[] buckets = new ArrayList[10];
        for (int i = 0; i < 10; i++) {
            buckets[i] = new ArrayList<>();
        }

        for (int i = 0; i < n; i++) {
            if (task.isCancelled()) return;

            int value = arr[i];
            int digit = (value / exp) % 10;
            count[digit]++;
            buckets[digit].add(value);

            if (stepByStep) {
                visualizer.highlightBlock(i, CURRENT_ELEMENT_COLOR);
                visualizer.updateStatus(String.format("Element %d has digit %d at current position, count[%d] = %d",
                        value, digit, digit, count[digit]));
                sleep(VERY_FAST_DELAY);
            }
        }

        if (stepByStep) {
            // Visualize the buckets
            showBuckets(buckets, count, visualizer);
            visualizer.updateStatus("🔢 Digit counts: " + Arrays.toString(count));
            sleep(STANDARD_DELAY);
        }

        // Step 2: Calculate cumulative count
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(4); // Calculate cumulative count
            visualizer.updateStatus("📈 Step 3: Calculate cumulative counts to determine positions");
            sleep(FAST_DELAY);
        }

        for (int i = 1; i < 10; i++) {
            count[i] += count[i - 1];

            if (stepByStep) {
                visualizer.updateStatus(String.format("Count[%d] = Count[%d] + Count[%d-1] = %d + %d = %d",
                        i, i, i, count[i], count[i-1], count[i]));
                sleep(VERY_FAST_DELAY);
            }
        }

        if (stepByStep) {
            visualizer.updateStatus("📊 Cumulative counts: " + Arrays.toString(count));
            sleep(STANDARD_DELAY);

            visualizer.highlightPseudocodeLine(5); // Build output array
            visualizer.updateStatus("🔄 Step 4: Build output array using digit-based positions");
            sleep(FAST_DELAY);
        }

        // Step 3: Build output array (in reverse order for stability)
        for (int i = n - 1; i >= 0; i--) {
            if (task.isCancelled()) return;

            int value = arr[i];
            int digit = (value / exp) % 10;

            // Calculate position in output array
            int position = count[digit] - 1;
            output[position] = value;

            if (stepByStep) {
                visualizer.highlightBlock(i, CURRENT_ELEMENT_COLOR);
                visualizer.updateStatus(String.format("Place %d (digit %d) at position count[%d]-1 = %d in output array",
                        value, digit, digit, position));
                sleep(FAST_DELAY);

                // Visualize the element movement
                visualizer.displayArray(output);
                visualizer.highlightBlock(position, BUCKET_COLOR);
                sleep(VERY_FAST_DELAY);
            }

            count[digit]--;
        }

        // Step 4: Copy output back to original array
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(6); // Copy back to original array
            visualizer.updateStatus("🔄 Step 5: Copy sorted output back to original array");
            sleep(FAST_DELAY);
        }

        System.arraycopy(output, 0, arr, 0, n);

        if (stepByStep) {
            visualizer.displayArray(arr);
            visualizer.updateStatus("👍 Array now sorted by digit at 10^" + ((int)Math.log10(exp)) + "'s place");
            sleep(STANDARD_DELAY);
        }
    }

    /**
     * Visualizes the buckets filled with elements based on their digit.
     */
    private static void showBuckets(List<Integer>[] buckets, int[] count, VisualizerPanel visualizer) {
        StringBuilder bucketString = new StringBuilder("🧺 Buckets:\n");

        for (int i = 0; i < 10; i++) {
            bucketString.append("Digit ").append(i).append(" (").append(count[i])
                    .append(" elements): ").append(buckets[i]).append("\n");
        }

        visualizer.updateStatus(bucketString.toString());
    }

    /**
     * Highlights the specific digit position being processed in each number.
     */
    private static void highlightDigitPosition(int[] arr, int exp, VisualizerPanel visualizer) {
        for (int i = 0; i < arr.length; i++) {
            int value = arr[i];
            int digit = (value / exp) % 10;

            // Display the current digit being processed
            visualizer.highlightBlock(i, CURRENT_DIGIT_COLOR);

            // You could potentially highlight just the specific digit within the number
            // if your visualization system supports it
            visualizer.updateStatus(String.format("Element %d has digit %d at position 10^%d",
                    value, digit, (int)Math.log10(exp)));
            sleep(100);
        }
    }

    /**
     * Returns the ordinal number string (1st, 2nd, 3rd, etc.)
     */
    private static String getOrdinalNumber(int n) {
        if (n == 1) return "1st";
        if (n == 2) return "2nd";
        if (n == 3) return "3rd";
        return n + "th";
    }

    /**
     * Updates the progress in the task.
     */
    private static void updateProgress(Task<?> task, double current, double total) {
        if (task != null && total > 0) {
            try {
                // Use reflection to access protected method
                java.lang.reflect.Method method = Task.class.getDeclaredMethod(
                        "updateProgress", double.class, double.class);
                method.setAccessible(true);
                method.invoke(task, current, total);
            } catch (Exception e) {
                // Silently ignore if progress can't be updated
            }
        }
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

