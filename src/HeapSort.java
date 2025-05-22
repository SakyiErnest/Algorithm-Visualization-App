import javafx.concurrent.Task;
import javafx.scene.paint.Color;

/**
 * Educational implementation of the Heap Sort algorithm with visualization.
 * Heap Sort builds a max heap and then repeatedly extracts the maximum element.
 */
public class HeapSort {
    // Color scheme for visualization
    private static final Color HEAP_NODE_COLOR = Color.web("#8b5cf6");    // Purple for heap nodes
    private static final Color CURRENT_NODE_COLOR = Color.web("#f59e0b"); // Amber for current node
    private static final Color LARGEST_COLOR = Color.web("#ef4444");      // Red for largest element
    private static final Color HEAPIFIED_COLOR = Color.web("#60a5fa");    // Blue for heapified subtree
    private static final Color SORTED_COLOR = Color.web("#38a169");       // Green for sorted elements
    
    /**
     * Sorts the array using Heap Sort algorithm with enhanced visualization.
     *
     * @param array       the array to sort
     * @param stepByStep  if true, sends animated updates to the visualizer
     * @param visualizer  the VisualizerPanel used to display steps
     * @param task        the current Task for cancellation checking and progress updates
     */
    public static void sort(int[] array, boolean stepByStep, VisualizerPanel visualizer, Task<?> task) {
        int n = array.length;
        
        // Display the initial array state
        if (stepByStep) {
            visualizer.displayArray(array);
            visualizer.updateStatus("🚀 Starting Heap Sort - an efficient comparison-based algorithm");
            visualizer.highlightPseudocodeLine(0); // Highlight algorithm title
            sleep(800);
            
            // Educational introduction
            visualizer.updateStatus("📚 Heap Sort first builds a max heap, then extracts elements one by one");
            sleep(1200);
        }
        
        // Build max heap phase
        if (stepByStep) {
            visualizer.updateStatus("🔨 Phase 1: Building the max heap");
            visualizer.highlightPseudocodeLine(1); // Build max heap
            sleep(800);
        }
        
        // Build heap (rearrange array)
        for (int i = n / 2 - 1; i >= 0; i--) {
            if (task.isCancelled()) return;
            
            if (stepByStep) {
                visualizer.updateStatus("🏗️ Building heap: heapifying subtree rooted at index " + i);
                visualizer.highlightBlock(i, CURRENT_NODE_COLOR);
                sleep(600);
            }
            
            heapify(array, n, i, stepByStep, visualizer, task);
        }
        
        // Extract elements from heap one by one
        if (stepByStep) {
            visualizer.updateStatus("🔄 Phase 2: Extracting elements from the heap");
            visualizer.highlightPseudocodeLine(2); // Extract elements one by one
            
            // Show the heap structure
            for (int i = 0; i < n; i++) {
                visualizer.highlightBlock(i, HEAP_NODE_COLOR);
                sleep(100);
            }
            sleep(600);
        }
        
        for (int i = n - 1; i > 0; i--) {
            if (task.isCancelled()) return;
            
            if (stepByStep) {
                visualizer.highlightPseudocodeLine(3); // Swap root (maximum) with end
                visualizer.updateStatus("📤 Extracting maximum element " + array[0] + " from heap");
                visualizer.highlightBlock(0, LARGEST_COLOR);
                visualizer.highlightBlock(i, CURRENT_NODE_COLOR);
                sleep(600);
            }
            
            // Swap root (max element) with the last element
            int temp = array[0];
            array[0] = array[i];
            array[i] = temp;
            
            if (stepByStep) {
                visualizer.animateSwap(0, i);
                visualizer.updateStatus("🔀 Swapped max element " + temp + " to position " + i);
                sleep(600);
                
                // Mark the element as sorted
                visualizer.highlightBlock(i, SORTED_COLOR);
                visualizer.updateStatus("✅ Element " + array[i] + " is now in its final sorted position");
                sleep(400);
            }
            
            // Heapify the reduced heap
            if (stepByStep) {
                visualizer.highlightPseudocodeLine(4); // Heapify the reduced heap
                visualizer.updateStatus("🔨 Restoring max heap property after extraction");
                sleep(500);
            }
            
            heapify(array, i, 0, stepByStep, visualizer, task);
            
            // Report progress
            updateProgress(task, n - i, n);
        }
        
        // Mark the first element as sorted (the smallest element)
        if (stepByStep) {
            visualizer.highlightBlock(0, SORTED_COLOR);
            
            // Final state - all sorted
            visualizer.displayArray(array);
            visualizer.updateStatus("✨ Heap Sort complete! All " + n + " elements are sorted.");
            
            // Final educational message
            sleep(800);
            visualizer.updateStatus("📊 Heap Sort: Time complexity O(n log n), Space complexity O(1). " +
                    "Efficient for large datasets.");
        }
    }
    
    /**
     * Heapifies a subtree rooted at node i which is an index in array[].
     * n is the size of heap.
     */
    private static void heapify(int[] array, int n, int i, boolean stepByStep, 
                              VisualizerPanel visualizer, Task<?> task) {
        if (task.isCancelled()) return;
        
        int largest = i;      // Initialize largest as root
        int left = 2 * i + 1; // Left child
        int right = 2 * i + 2; // Right child
        
        if (stepByStep) {
            visualizer.highlightPseudocodeLine(6); // Find largest among root, left child and right child
            visualizer.updateStatus("🔍 Checking node at index " + i + " with value " + array[i]);
            sleep(400);
        }
        
        // If left child is larger than root
        if (left < n) {
            if (stepByStep) {
                visualizer.updateStatus("👈 Checking left child at index " + left + 
                                       " with value " + array[left]);
                visualizer.highlightBlock(left, CURRENT_NODE_COLOR);
                sleep(500);
            }
            
            if (array[left] > array[largest]) {
                if (stepByStep) {
                    visualizer.updateStatus("📈 Left child " + array[left] + 
                                           " is larger than current largest " + array[largest]);
                    visualizer.highlightBlock(left, LARGEST_COLOR);
                    sleep(500);
                }
                largest = left;
            } else if (stepByStep) {
                visualizer.resetBlockColor(left);
            }
        }
        
        // If right child is larger than largest so far
        if (right < n) {
            if (stepByStep) {
                visualizer.updateStatus("👉 Checking right child at index " + right + 
                                       " with value " + array[right]);
                visualizer.highlightBlock(right, CURRENT_NODE_COLOR);
                sleep(500);
            }
            
            if (array[right] > array[largest]) {
                if (stepByStep) {
                    // If previous largest was left child, reset its color
                    if (largest != i) {
                        visualizer.resetBlockColor(largest);
                    }
                    
                    visualizer.updateStatus("📈 Right child " + array[right] + 
                                           " is larger than current largest " + array[largest]);
                    visualizer.highlightBlock(right, LARGEST_COLOR);
                    sleep(500);
                }
                largest = right;
            } else if (stepByStep) {
                visualizer.resetBlockColor(right);
            }
        }
        
        // If largest is not root
        if (largest != i) {
            if (stepByStep) {
                visualizer.highlightPseudocodeLine(7); // If largest is not root, swap
                visualizer.updateStatus("🔄 Node " + array[i] + " at index " + i + 
                                       " is not the largest. Swapping with " + array[largest]);
                sleep(600);
            }
            
            // Swap
            int temp = array[i];
            array[i] = array[largest];
            array[largest] = temp;
            
            if (stepByStep) {
                visualizer.animateSwap(i, largest);
                sleep(600);
            }
            
            // Recursively heapify the affected sub-tree
            if (stepByStep) {
                visualizer.highlightPseudocodeLine(8); // Recursively heapify the affected subtree
                visualizer.updateStatus("🔁 Recursively heapifying the affected subtree rooted at index " + largest);
                sleep(500);
            }
            
            // Recursively heapify the affected sub-tree
            heapify(array, n, largest, stepByStep, visualizer, task);
        } else if (stepByStep) {
            visualizer.updateStatus("✓ Node at index " + i + " is already the largest in its subtree");
            visualizer.highlightBlock(i, HEAPIFIED_COLOR);
            sleep(400);
        }
        
        if (stepByStep) {
            visualizer.updateStatus("✅ Subtree rooted at index " + i + " has been heapified");
        }
    }
    
    /**
     * Updates the progress in the task using reflection to access protected method.
     */
    private static void updateProgress(Task<?> task, double current, double total) {
        if (task != null && total > 0) {
            try {
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
     * Pauses execution for visualization purposes.
     */
    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 