import javafx.concurrent.Task;
import javafx.scene.paint.Color;

/**
 * Educational implementation of the Linear Search algorithm with enhanced visualizations.
 * Linear Search sequentially examines each element in a collection until finding the target.
 */
public class LinearSearch {
    // Enhanced color scheme for better visual understanding
    private static final Color CURRENT_ELEMENT_COLOR = Color.web("#f59e0b");    // Amber for current element
    private static final Color EXAMINED_ELEMENT_COLOR = Color.web("#94a3b8");   // Gray for examined elements
    private static final Color FOUND_ELEMENT_COLOR = Color.web("#10b981");      // Bright green for found element
    private static final Color NOT_FOUND_COLOR = Color.web("#ef4444");          // Red for not found indication
    private static final Color REMAINING_COLOR = Color.web("#60a5fa");          // Blue for unexamined elements

    /**
     * Searches for an element in the array using a linear search with enhanced visualization.
     *
     * @param array            the array to search
     * @param elementToSearch  the element to search for
     * @param stepByStep       if true, sends animated updates to the visualizer for each step
     * @param visualizer       the VisualizerPanel used to display steps
     * @param task             the current Task for cancellation checking and progress updates
     * @return the index of the element if found, or -1 if not found
     */
    public static int search(int[] array, int elementToSearch, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        if (stepByStep) {
            // Show the initial array state and status
            visualizer.displayArray(array);
            visualizer.updateStatus("🚀 Starting Linear Search for element: " + elementToSearch);
            visualizer.highlightPseudocodeLine(0); // Highlight algorithm title in pseudocode
            sleep(1000);

            // Educational introduction
            visualizer.updateStatus("📚 Linear Search checks each element sequentially until finding the target");
            sleep(1200);

            visualizer.highlightPseudocodeLine(1); // for i = 0 to n-1
        }

        // Main search loop
        for (int index = 0; index < array.length; index++) {
            // Update search progress
            updateProgress(task, index, array.length);

            if (stepByStep) {
                // Highlight the current element being examined
                visualizer.highlightBlock(index, CURRENT_ELEMENT_COLOR);
                visualizer.highlightPseudocodeLine(2); // if array[i] == target

                String comparisonMessage = String.format("🔍 Examining index %d: Is %d == %d?",
                        index, array[index], elementToSearch);
                visualizer.updateStatus(comparisonMessage);
                sleep(600);

                // Show the progress through the array
                String progressMessage = String.format("⏱️ Progress: Checked %d of %d elements (%.1f%%)",
                        index + 1, array.length,
                        (index + 1) * 100.0 / array.length);
                visualizer.updateStatus(progressMessage);

                if (task.isCancelled()) {
                    return -1;
                }

                // Show educational message about time complexity at certain points
                if (index == array.length / 4) {
                    visualizer.updateStatus("💡 Linear Search: Best case is O(1) if the element is found immediately");
                    sleep(1000);
                } else if (index == array.length / 2) {
                    visualizer.updateStatus("💡 Linear Search: Average case requires checking n/2 elements");
                    sleep(1000);
                } else if (index == 3 * array.length / 4) {
                    visualizer.updateStatus("💡 Linear Search: Worst case is O(n) if the element is last or not present");
                    sleep(1000);
                }
            }

            // Check if the current element matches the search target
            if (array[index] == elementToSearch) {
                if (stepByStep) {
                    // Highlight the found element
                    visualizer.highlightBlock(index, FOUND_ELEMENT_COLOR);
                    visualizer.highlightPseudocodeLine(3); // return i

                    // Show successful result with animated celebration
                    String foundMessage = String.format("✅ SUCCESS! Found %d at index %d after examining %d elements",
                            elementToSearch, index, index + 1);
                    visualizer.updateStatus(foundMessage);

                    // Show educational message about efficiency
                    if (index < array.length / 3) {
                        sleep(800);
                        visualizer.updateStatus("🎯 Got lucky! Found the element early in the array");
                    } else if (index < 2 * array.length / 3) {
                        sleep(800);
                        visualizer.updateStatus("👍 Average case: Found the element near the middle of the array");
                    } else {
                        sleep(800);
                        visualizer.updateStatus("⚠️ Inefficient case: Had to search most of the array to find the element");
                    }

                    sleep(1000);

                    // Final educational message
                    visualizer.updateStatus("📊 Time Complexity: O(n) | Space Complexity: O(1)");
                }

                // Update final progress
                updateProgress(task, array.length, array.length);
                return index;
            }

            if (stepByStep) {
                // Mark the current element as examined and move on
                visualizer.highlightBlock(index, EXAMINED_ELEMENT_COLOR);
                visualizer.highlightPseudocodeLine(1); // Back to the loop
                sleep(400);
            }
        }

        // Element not found
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(5); // return -1

            // Show all elements as examined
            for (int i = 0; i < array.length; i++) {
                visualizer.highlightBlock(i, NOT_FOUND_COLOR);
            }

            visualizer.updateStatus("❌ Element " + elementToSearch + " not found after examining all " + array.length + " elements");
            sleep(800);

            // Educational message about worst case
            visualizer.updateStatus("⚠️ This represents the worst-case scenario for Linear Search: O(n)");
            sleep(1000);

            // Final educational message
            visualizer.updateStatus("📊 Linear Search is simple but inefficient for large arrays. Consider Binary Search for sorted data.");
        }

        // Update final progress
        updateProgress(task, array.length, array.length);
        return -1;
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