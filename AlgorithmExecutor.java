/* File: AlgorithmExecutor.java */
import javafx.concurrent.Task;

import java.util.Arrays;

public class AlgorithmExecutor {
    /**
     * Executes the given algorithm on the array. It calls your specific algorithm class.
     * If stepByStep is enabled, your algorithm implementations should update the visualizer.
     * Adjust the method calls below to match your actual class methods if necessary.
     */
    public static String executeAlgorithm(int[] arr, String algorithm, int target, boolean stepByStep,
                                          VisualizerPanel visualizer, Task<?> task) {
        int[] originalArray = arr.clone();
        String result;
        String timeComplexity = getTimeComplexity(algorithm);
        long startTime = System.nanoTime();

        switch (algorithm) {
            case "Selection Sort":
                // Call your dedicated SelectionSort class
                SelectionSort.sort(arr, stepByStep, visualizer, task);
                result = formatSortResult(originalArray, arr, "Selection Sort");
                break;
            case "Insertion Sort":
                InsertionSort.sort(arr, stepByStep, visualizer, task);
                result = formatSortResult(originalArray, arr, "Insertion Sort");
                break;
            case "Quick Sort":
                QuickSort.sort(arr, 0, arr.length - 1, stepByStep, visualizer, task);
                result = formatSortResult(originalArray, arr, "Quick Sort");
                break;
            case "Merge Sort":
                MergeSort.sort(arr, 0, arr.length - 1, stepByStep, visualizer, task);
                result = formatSortResult(originalArray, arr, "Merge Sort");
                break;
            case "Radix Sort":
                RadixSort.sort(arr, stepByStep, visualizer, task);
                result = formatSortResult(originalArray, arr, "Radix Sort");
                break;
            case "Linear Search":
                int searchIndexLS = LinearSearch.search(arr, target, stepByStep, visualizer, task);
                result = formatSearchResult(arr, target, searchIndexLS, algorithm);
                break;
            case "Binary Search":
                // For Binary Search, sort the array first (or assume it is already sorted)
                Arrays.sort(arr);
                int searchIndexBS = BinarySearch.search(arr, target, stepByStep, visualizer, task);
                result = formatSearchResult(arr, target, searchIndexBS, algorithm);
                break;
            default:
                result = "Invalid algorithm selection";
        }
        long endTime = System.nanoTime();
        double executionTime = (endTime - startTime) / 1_000_000.0;
        return result + String.format("\nTime Complexity: %s\nExecution Time: %.2f ms", timeComplexity, executionTime);
    }

    private static String getTimeComplexity(String algorithm) {
        return switch (algorithm) {
            case "Selection Sort" -> "O(n²) - Quadratic time";
            case "Insertion Sort" -> "O(n²) - Quadratic time (O(n) for nearly sorted arrays)";
            case "Quick Sort" -> "O(n log n) average, O(n²) worst case";
            case "Merge Sort" -> "O(n log n) - Consistent performance";
            case "Radix Sort" -> "O(d * n) where d is the number of digits";
            case "Linear Search" -> "O(n) - Linear time";
            case "Binary Search" -> "O(log n) - Logarithmic time";
            default -> "Unknown";
        };
    }

    private static String formatSortResult(int[] original, int[] sorted, String algorithm) {
        return String.format("Algorithm: %s\nOriginal array: %s\nSorted array: %s",
                algorithm, Arrays.toString(original), Arrays.toString(sorted));
    }

    private static String formatSearchResult(int[] arr, int target, int result, String algorithm) {
        return String.format("Algorithm: %s\nArray: %s\nTarget: %d\nResult: %s",
                algorithm, Arrays.toString(arr), target, result != -1 ? "Found at index " + result : "Element not found");
    }
}
