import javafx.concurrent.Task;
import javafx.scene.paint.Color;

public class LinearSearch {
    /**
     * Searches for an element in the array using a linear search.
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
            // Show the initial array state and status.
            visualizer.displayArray(array);
            visualizer.updateStatus("Starting Linear Search");
            sleep(300);
        }

        for (int index = 0; index < array.length; index++) {
            if (stepByStep) {
                // Highlight the current index being examined.
                visualizer.highlightBlock(index, Color.YELLOW);
                visualizer.updateStatus("Checking index " + index + " (value: " + array[index] + ")");
                sleep(200);

                if (task.isCancelled()) {
                    return -1;
                }
                // Reset the block color back to default (assuming LIGHTBLUE is the default).
                visualizer.highlightBlock(index, Color.LIGHTBLUE);
            }

            if (array[index] == elementToSearch) {
                if (stepByStep) {
                    // Highlight the found element.
                    visualizer.highlightBlock(index, Color.LIMEGREEN);
                    visualizer.updateStatus("Found " + elementToSearch + " at index " + index);
                    sleep(300);
                }
                return index;
            }
        }
        if (stepByStep) {
            visualizer.updateStatus("Element " + elementToSearch + " not found.");
        }
        return -1;
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
