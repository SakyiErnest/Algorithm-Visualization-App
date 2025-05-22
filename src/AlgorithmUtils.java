import javafx.concurrent.Task;

/**
 * Utility class for algorithm implementations providing safe task operations 
 * and other common utilities.
 */
public class AlgorithmUtils {
    
    /**
     * Safely checks if a task is cancelled, handling null tasks gracefully.
     * 
     * @param task The task to check, may be null
     * @return true if the task is cancelled, false otherwise or if task is null
     */
    public static boolean isCancelled(Task<?> task) {
        return task != null && task.isCancelled();
    }
    
    /**
     * Safely updates the progress of a task.
     * 
     * @param task The task to update, may be null
     * @param current Current progress value
     * @param total Total progress value
     */
    public static void updateProgress(Task<?> task, double current, double total) {
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
     * Safely sleeps for the specified milliseconds.
     * 
     * @param millis Time to sleep in milliseconds
     */
    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 