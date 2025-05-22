import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Interactive visualization tool for algorithm time complexity analysis.
 * Shows how different algorithms scale with input size.
 */
public class AlgorithmComplexityVisualizer {
    
    private BorderPane mainPane;
    private LineChart<Number, Number> complexityChart;
    private TableView<AlgorithmData> resultTable;
    private VBox controlPanel;
    private Button runAnalysisButton;
    private ProgressBar progressBar;
    private Label statusLabel;
    private ComboBox<String> inputTypesComboBox;
    private TextField arraySizeField;
    private TextField iterationsField;
    private CheckBox logarithmicScaleCheckBox;
    
    private Map<String, SimpleBooleanProperty> selectedAlgorithms = new HashMap<>();
    private final NumberAxis xAxis;
    private final NumberAxis yAxis;
    private ExecutorService executor;
    
    // Input size presets for testing
    private final int[] INPUT_SIZES = {10, 100, 500, 1000, 2000, 5000, 7500, 10000};
    private final String[] INPUT_TYPES = {"Random", "Nearly Sorted", "Reverse Sorted"};
    private final String[] ALGORITHMS = {
            "Bubble Sort", "Selection Sort", "Insertion Sort", 
            "Quick Sort", "Merge Sort", "Heap Sort",
            "Counting Sort", "Radix Sort",
            "Linear Search", "Binary Search"
    };
    
    // Color scheme for different algorithms
    private final String[] COLORS = {
            "#ff7e67", "#feb72b", "#5c95ff", "#0be881", 
            "#9980fa", "#badc58", "#f368e0", "#ff9f43",
            "#10ac84", "#222f3e"
    };
    
    // Theoretical big-O functions for plotting
    private final Map<String, double[]> THEORETICAL_COMPLEXITY = new HashMap<>();
    
    public AlgorithmComplexityVisualizer() {
        executor = Executors.newSingleThreadExecutor();
        
        // Setup the theoretical complexity functions
        initializeTheoreticalComplexities();
        
        // Create the main layout container
        mainPane = new BorderPane();
        mainPane.setPadding(new Insets(15));
        
        // Setup chart axes
        xAxis = new NumberAxis();
        xAxis.setLabel("Input Size (n)");
        yAxis = new NumberAxis();
        yAxis.setLabel("Execution Time (ms)");
        
        // Create chart for visualization
        complexityChart = new LineChart<>(xAxis, yAxis);
        complexityChart.setTitle("Algorithm Complexity Analysis");
        complexityChart.setAnimated(false);
        complexityChart.setCreateSymbols(true);
        
        // Create control panel
        setupControlPanel();
        
        // Create results table
        setupResultsTable();
        
        // Layout
        mainPane.setCenter(complexityChart);
        mainPane.setRight(controlPanel);
        mainPane.setBottom(resultTable);
        
        // Style
        applyStyles();
    }
    
    private void initializeTheoreticalComplexities() {
        // Initialize the map of theoretical complexities
        for (String algorithm : ALGORITHMS) {
            double[] complexity = new double[INPUT_SIZES.length];
            for (int i = 0; i < INPUT_SIZES.length; i++) {
                int n = INPUT_SIZES[i];
                double scaleFactor = 0.01; // Adjust this to make curves visible
                
                switch (algorithm) {
                    case "Bubble Sort":
                    case "Selection Sort":
                        // O(n²)
                        complexity[i] = scaleFactor * n * n;
                        break;
                    case "Insertion Sort":
                        // O(n²) but with better constant factors
                        complexity[i] = scaleFactor * 0.5 * n * n;
                        break;
                    case "Quick Sort":
                    case "Merge Sort":
                    case "Heap Sort":
                        // O(n log n)
                        complexity[i] = scaleFactor * n * Math.log(n);
                        break;
                    case "Counting Sort":
                    case "Radix Sort":
                        // O(n)
                        complexity[i] = scaleFactor * 2 * n;
                        break;
                    case "Linear Search":
                        // O(n)
                        complexity[i] = scaleFactor * n;
                        break;
                    case "Binary Search":
                        // O(log n)
                        complexity[i] = scaleFactor * 5 * Math.log(n);
                        break;
                }
            }
            THEORETICAL_COMPLEXITY.put(algorithm, complexity);
        }
    }
    
    private void setupControlPanel() {
        controlPanel = new VBox(15);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setPrefWidth(280);
        controlPanel.getStyleClass().add("control-panel");
        
        // Title for control panel
        Label titleLabel = new Label("Complexity Analysis Controls");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // Input type selection
        Label inputTypeLabel = new Label("Input Type:");
        inputTypesComboBox = new ComboBox<>(FXCollections.observableArrayList(INPUT_TYPES));
        inputTypesComboBox.getSelectionModel().selectFirst();
        inputTypesComboBox.setMaxWidth(Double.MAX_VALUE);
        
        // Custom input sizes
        Label arraySizeLabel = new Label("Custom Array Size (comma separated):");
        arraySizeField = new TextField("10, 100, 500, 1000, 5000");
        arraySizeField.setPromptText("e.g. 10, 100, 1000");
        
        // Number of iterations for averaging
        Label iterationsLabel = new Label("Test Iterations:");
        iterationsField = new TextField("3");
        iterationsField.setPromptText("Number of runs to average");
        
        // Logarithmic scale option
        logarithmicScaleCheckBox = new CheckBox("Use Logarithmic Scale");
        logarithmicScaleCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
                    @Override
                    public String toString(Number object) {
                        return String.format("%.1f", Math.log10(object.doubleValue() + 1));
                    }
                });
            } else {
                yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis));
            }
            refreshChart();
        });
        
        // Algorithm selection checkboxes
        TitledPane algorithmsPane = createAlgorithmCheckboxes();
        
        // Run analysis button
        runAnalysisButton = new Button("Run Complexity Analysis");
        runAnalysisButton.setMaxWidth(Double.MAX_VALUE);
        runAnalysisButton.getStyleClass().add("button-primary");
        runAnalysisButton.setOnAction(e -> runComplexityAnalysis());
        
        // Status components
        progressBar = new ProgressBar(0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setVisible(false);
        
        statusLabel = new Label("Ready");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        
        // Add all controls to the panel
        controlPanel.getChildren().addAll(
                titleLabel,
                inputTypeLabel, inputTypesComboBox,
                arraySizeLabel, arraySizeField,
                iterationsLabel, iterationsField,
                logarithmicScaleCheckBox,
                algorithmsPane,
                runAnalysisButton,
                progressBar,
                statusLabel
        );
    }
    
    private TitledPane createAlgorithmCheckboxes() {
        VBox checkboxContainer = new VBox(8);
        checkboxContainer.setPadding(new Insets(10));
        
        // Create a checkbox for each algorithm
        for (int i = 0; i < ALGORITHMS.length; i++) {
            String algorithm = ALGORITHMS[i];
            CheckBox cb = new CheckBox(algorithm);
            
            // Default-select the first few algorithms
            boolean isSelected = i < 4;
            cb.setSelected(isSelected);
            
            SimpleBooleanProperty selectedProperty = new SimpleBooleanProperty(isSelected);
            selectedAlgorithms.put(algorithm, selectedProperty);
            
            cb.selectedProperty().bindBidirectional(selectedProperty);
            cb.setUserData(COLORS[i % COLORS.length]);
            
            checkboxContainer.getChildren().add(cb);
        }
        
        TitledPane algorithmsPane = new TitledPane("Select Algorithms", checkboxContainer);
        algorithmsPane.setExpanded(true);
        return algorithmsPane;
    }
    
    private void setupResultsTable() {
        resultTable = new TableView<>();
        resultTable.setPrefHeight(200);
        
        // Define columns
        TableColumn<AlgorithmData, String> algorithmCol = new TableColumn<>("Algorithm");
        algorithmCol.setCellValueFactory(data -> data.getValue().algorithmProperty());
        algorithmCol.setPrefWidth(150);
        
        TableColumn<AlgorithmData, String> complexityCol = new TableColumn<>("Time Complexity");
        complexityCol.setCellValueFactory(data -> data.getValue().complexityProperty());
        complexityCol.setPrefWidth(150);
        
        TableColumn<AlgorithmData, String> avgTimeCol = new TableColumn<>("Avg. Time (ms)");
        avgTimeCol.setCellValueFactory(data -> data.getValue().avgTimeProperty());
        avgTimeCol.setPrefWidth(120);
        
        TableColumn<AlgorithmData, String> consistencyCol = new TableColumn<>("Consistency");
        consistencyCol.setCellValueFactory(data -> data.getValue().consistencyProperty());
        consistencyCol.setPrefWidth(120);
        
        TableColumn<AlgorithmData, String> scaleFactorCol = new TableColumn<>("Scaling Factor");
        scaleFactorCol.setCellValueFactory(data -> data.getValue().scaleFactorProperty());
        scaleFactorCol.setPrefWidth(120);
        
        resultTable.getColumns().addAll(algorithmCol, complexityCol, avgTimeCol, consistencyCol, scaleFactorCol);
        
        // Add some sample data
        ObservableList<AlgorithmData> data = FXCollections.observableArrayList(
                new AlgorithmData("Bubble Sort", "O(n²)", "-", "-", "-"),
                new AlgorithmData("Quick Sort", "O(n log n)", "-", "-", "-"),
                new AlgorithmData("Merge Sort", "O(n log n)", "-", "-", "-"),
                new AlgorithmData("Binary Search", "O(log n)", "-", "-", "-")
        );
        resultTable.setItems(data);
    }
    
    private void applyStyles() {
        mainPane.getStyleClass().add("complexity-visualizer");
        controlPanel.getStyleClass().add("input-panel");
        
        // Add any additional styling
        runAnalysisButton.getStyleClass().add("button-primary");
    }
    
    // Add a dummy task class that will be used when running algorithms
    private class DummyTask extends Task<Void> {
        @Override
        protected Void call() {
            return null;
        }
    }

    private void runComplexityAnalysis() {
        // Reset UI
        progressBar.setVisible(true);
        progressBar.setProgress(-1); // Indeterminate
        statusLabel.setText("Running analysis...");
        runAnalysisButton.setDisable(true);
        complexityChart.getData().clear();
        
        // Parse custom array sizes if provided
        int[] inputSizes = parseInputSizes();
        if (inputSizes.length == 0) {
            statusLabel.setText("Invalid input sizes");
            progressBar.setVisible(false);
            runAnalysisButton.setDisable(false);
            return;
        }
        
        // Get number of iterations
        int iterations = parseIterations();
        
        // Get selected input type
        String inputType = inputTypesComboBox.getValue();
        
        // Get selected algorithms
        List<String> algorithmsToRun = new ArrayList<>();
        for (Map.Entry<String, SimpleBooleanProperty> entry : selectedAlgorithms.entrySet()) {
            if (entry.getValue().get()) {
                algorithmsToRun.add(entry.getKey());
            }
        }
        
        // Run the analysis in background
        executor.submit(() -> {
            try {
                // Results data
                Map<String, XYChart.Series<Number, Number>> results = new HashMap<>();
                Map<String, Map<Integer, List<Long>>> timings = new HashMap<>();
                
                // Create a dummy task to pass to algorithms
                Task<Void> dummyTask = new DummyTask();
                
                // Initialize results structures
                for (String algorithm : algorithmsToRun) {
                    XYChart.Series<Number, Number> series = new XYChart.Series<>();
                    series.setName(algorithm);
                    results.put(algorithm, series);
                    timings.put(algorithm, new HashMap<>());
                }
                
                // Run tests for each input size
                for (int size : inputSizes) {
                    for (int iter = 0; iter < iterations; iter++) {
                        final String statusMessage = String.format("Testing size %d, iteration %d/%d", size, iter+1, iterations);
                        Platform.runLater(() -> statusLabel.setText(statusMessage));
                        
                        // Generate test array based on input type
                        int[] testArray = generateTestArray(size, inputType);
                        
                        // Test each selected algorithm
                        for (String algorithm : algorithmsToRun) {
                            // Skip search algorithms for very large arrays to prevent long wait times
                            if ((algorithm.equals("Linear Search") || algorithm.equals("Binary Search")) && size > 20000) {
                                continue;
                            }
                            
                            // Clone array to prevent modifications affecting other tests
                            int[] arrayCopy = testArray.clone();
                            
                            // Measure execution time
                            long startTime = System.nanoTime();
                            
                            // Run the algorithm (without visualization but with dummy task)
                            if (algorithm.equals("Binary Search")) {
                                // First sort for binary search
                                Arrays.sort(arrayCopy);
                                int target = arrayCopy[arrayCopy.length / 2]; // Middle element as target
                                BinarySearch.search(arrayCopy, target, false, null, dummyTask);
                            } else if (algorithm.equals("Linear Search")) {
                                int target = arrayCopy[arrayCopy.length / 2]; // Middle element as target
                                LinearSearch.search(arrayCopy, target, false, null, dummyTask);
                            } else {
                                // For sorting algorithms
                                runSortingAlgorithm(algorithm, arrayCopy, dummyTask);
                            }
                            
                            long endTime = System.nanoTime();
                            long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds
                            
                            // Store the timing
                            timings.get(algorithm).computeIfAbsent(size, k -> new ArrayList<>()).add(duration);
                        }
                    }
                }
                
                // Calculate average timings and update the chart
                for (String algorithm : algorithmsToRun) {
                    XYChart.Series<Number, Number> series = results.get(algorithm);
                    
                    for (int size : inputSizes) {
                        List<Long> algorithmTimings = timings.get(algorithm).get(size);
                        if (algorithmTimings != null && !algorithmTimings.isEmpty()) {
                            long sum = 0;
                            for (Long timing : algorithmTimings) {
                                sum += timing;
                            }
                            double avg = (double) sum / algorithmTimings.size();
                            
                            // Add data point to series
                            series.getData().add(new XYChart.Data<>(size, avg));
                        }
                    }
                }
                
                // Update UI from the JavaFX thread
                Platform.runLater(() -> {
                    // Add each series to the chart
                    for (int i = 0; i < algorithmsToRun.size(); i++) {
                        String algorithm = algorithmsToRun.get(i);
                        XYChart.Series<Number, Number> series = results.get(algorithm);
                        
                        if (!series.getData().isEmpty()) {
                            complexityChart.getData().add(series);
                            
                            // Apply colors
                            String color = COLORS[i % COLORS.length];
                            applySeriesColor(series, color);
                        }
                    }
                    
                    // Add theoretical complexity lines
                    if (algorithmsToRun.size() <= 3) { // Only add for clarity when few algorithms selected
                        addTheoreticalComplexityCurves(algorithmsToRun, inputSizes);
                    }
                    
                    // Update the result table
                    updateResultTable(algorithmsToRun, timings, inputSizes);
                    
                    // Reset UI
                    progressBar.setVisible(false);
                    statusLabel.setText("Analysis complete");
                    runAnalysisButton.setDisable(false);
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    progressBar.setVisible(false);
                    runAnalysisButton.setDisable(false);
                });
            }
        });
    }
    
    private void refreshChart() {
        // Store current series
        List<XYChart.Series<Number, Number>> currentSeries = new ArrayList<>(complexityChart.getData());
        
        // Clear and re-add
        complexityChart.getData().clear();
        complexityChart.getData().addAll(currentSeries);
    }
    
    private int[] parseInputSizes() {
        try {
            String[] sizeStrings = arraySizeField.getText().split(",");
            int[] sizes = new int[sizeStrings.length];
            
            for (int i = 0; i < sizeStrings.length; i++) {
                sizes[i] = Integer.parseInt(sizeStrings[i].trim());
                if (sizes[i] <= 0) throw new NumberFormatException("Sizes must be positive");
            }
            
            // Sort the sizes
            Arrays.sort(sizes);
            return sizes;
        } catch (NumberFormatException e) {
            return INPUT_SIZES; // Return default if parsing fails
        }
    }
    
    private int parseIterations() {
        try {
            int iterations = Integer.parseInt(iterationsField.getText().trim());
            return Math.max(1, Math.min(10, iterations)); // Between 1 and 10
        } catch (NumberFormatException e) {
            return 3; // Default
        }
    }
    
    private int[] generateTestArray(int size, String inputType) {
        int[] array = new int[size];
        
        switch (inputType) {
            case "Random":
                // Random array
                for (int i = 0; i < size; i++) {
                    array[i] = (int) (Math.random() * size * 10);
                }
                break;
                
            case "Nearly Sorted":
                // Create sorted array
                for (int i = 0; i < size; i++) {
                    array[i] = i;
                }
                
                // Swap ~5% of elements
                int swaps = size / 20;
                for (int i = 0; i < swaps; i++) {
                    int idx1 = (int) (Math.random() * size);
                    int idx2 = (int) (Math.random() * size);
                    int temp = array[idx1];
                    array[idx1] = array[idx2];
                    array[idx2] = temp;
                }
                break;
                
            case "Reverse Sorted":
                // Reverse sorted array
                for (int i = 0; i < size; i++) {
                    array[i] = size - i;
                }
                break;
                
            default:
                // Random as default
                for (int i = 0; i < size; i++) {
                    array[i] = (int) (Math.random() * size * 10);
                }
        }
        
        return array;
    }
    
    // Update method to accept Task parameter
    private void runSortingAlgorithm(String algorithm, int[] array, Task<?> task) {
        switch (algorithm) {
            case "Bubble Sort":
                BubbleSort.sort(array, false, null, task);
                break;
            case "Selection Sort":
                SelectionSort.sort(array, false, null, task);
                break;
            case "Insertion Sort":
                InsertionSort.sort(array, false, null, task);
                break;
            case "Quick Sort":
                QuickSort.sort(array, 0, array.length - 1, false, null, task);
                break;
            case "Merge Sort":
                MergeSort.sort(array, 0, array.length - 1, false, null, task);
                break;
            case "Heap Sort":
                HeapSort.sort(array, false, null, task);
                break;
            case "Counting Sort":
                CountingSort.sort(array, false, null, task);
                break;
            case "Radix Sort":
                RadixSort.sort(array, false, null, task);
                break;
        }
    }
    
    private void addTheoreticalComplexityCurves(List<String> algorithmsToRun, int[] inputSizes) {
        int maxSize = inputSizes[inputSizes.length - 1];
        
        for (String algorithm : algorithmsToRun) {
            XYChart.Series<Number, Number> theoreticalSeries = new XYChart.Series<>();
            theoreticalSeries.setName(algorithm + " (theoretical)");
            
            // Find the largest real timing for scaling
            double maxRealTiming = 0;
            for (XYChart.Series<Number, Number> series : complexityChart.getData()) {
                if (series.getName().equals(algorithm)) {
                    for (XYChart.Data<Number, Number> data : series.getData()) {
                        if (data.getYValue().doubleValue() > maxRealTiming) {
                            maxRealTiming = data.getYValue().doubleValue();
                        }
                    }
                }
            }
            
            if (maxRealTiming > 0) {
                // Get theoretical values
                double[] theoreticalValues = THEORETICAL_COMPLEXITY.get(algorithm);
                
                // Find a good scaling factor
                double maxTheoreticalValue = 0;
                for (double value : theoreticalValues) {
                    if (value > maxTheoreticalValue) {
                        maxTheoreticalValue = value;
                    }
                }
                
                // Calculate scaling factor to match real timings
                double scalingFactor = maxRealTiming / maxTheoreticalValue;
                
                // Create scaled theoretical curve
                for (int i = 0; i < INPUT_SIZES.length; i++) {
                    theoreticalSeries.getData().add(
                            new XYChart.Data<>(INPUT_SIZES[i], theoreticalValues[i] * scalingFactor));
                }
                
                complexityChart.getData().add(theoreticalSeries);
                
                // Style the theoretical line (dashed)
                for (XYChart.Series<Number, Number> series : complexityChart.getData()) {
                    if (series.getName().equals(algorithm + " (theoretical)")) {
                        // Find the corresponding algorithm to match colors
                        for (int i = 0; i < ALGORITHMS.length; i++) {
                            if (ALGORITHMS[i].equals(algorithm)) {
                                String color = COLORS[i % COLORS.length];
                                applyTheoryStyling(series, color);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
    
    private void applySeriesColor(XYChart.Series<Number, Number> series, String colorHex) {
        String style = "-fx-stroke: " + colorHex + ";";
        series.getNode().setStyle(style);
        
        // Style data points
        for (XYChart.Data<Number, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-background-color: " + colorHex + ";");
            }
        }
    }
    
    private void applyTheoryStyling(XYChart.Series<Number, Number> series, String colorHex) {
        String style = "-fx-stroke: " + colorHex + ";" +
                "-fx-stroke-dash-array: 5 5;"; // Dashed line
        series.getNode().setStyle(style);
        
        // Make data points invisible
        for (XYChart.Data<Number, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setVisible(false);
            }
        }
    }
    
    private void updateResultTable(List<String> algorithms, 
                                  Map<String, Map<Integer, List<Long>>> timings,
                                  int[] inputSizes) {
        ObservableList<AlgorithmData> data = FXCollections.observableArrayList();
        
        // Find largest input size for scaling ratio calculation
        Arrays.sort(inputSizes);
        int smallestSize = inputSizes[0];
        int largestSize = inputSizes[inputSizes.length - 1];
        
        for (String algorithm : algorithms) {
            // Skip if we don't have timing data
            if (!timings.containsKey(algorithm)) continue;
            Map<Integer, List<Long>> sizeToTimings = timings.get(algorithm);
            
            // Calculate average time across all sizes
            List<Double> averageTimes = new ArrayList<>();
            for (int size : inputSizes) {
                if (sizeToTimings.containsKey(size)) {
                    List<Long> times = sizeToTimings.get(size);
                    if (!times.isEmpty()) {
                        double avg = calculateAverage(times);
                        averageTimes.add(avg);
                    }
                }
            }
            
            if (averageTimes.isEmpty()) continue;
            
            // Calculate overall average for this algorithm
            double overallAvg = averageTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            
            // Calculate consistency (standard deviation / mean)
            double stdDev = 0;
            if (averageTimes.size() > 1) {
                double sumSquaredDiffs = 0;
                for (Double time : averageTimes) {
                    sumSquaredDiffs += Math.pow(time - overallAvg, 2);
                }
                stdDev = Math.sqrt(sumSquaredDiffs / averageTimes.size());
            }
            double consistencyRatio = overallAvg > 0 ? stdDev / overallAvg : 0;
            String consistencyRating;
            if (consistencyRatio < 0.1) {
                consistencyRating = "Excellent";
            } else if (consistencyRatio < 0.25) {
                consistencyRating = "Good";
            } else if (consistencyRatio < 0.5) {
                consistencyRating = "Fair";
            } else {
                consistencyRating = "Poor";
            }
            
            // Calculate growth ratio between smallest and largest input sizes
            double smallSizeAvg = 0;
            if (sizeToTimings.containsKey(smallestSize) && !sizeToTimings.get(smallestSize).isEmpty()) {
                smallSizeAvg = calculateAverage(sizeToTimings.get(smallestSize));
            }
            
            double largeSizeAvg = 0;
            if (sizeToTimings.containsKey(largestSize) && !sizeToTimings.get(largestSize).isEmpty()) {
                largeSizeAvg = calculateAverage(sizeToTimings.get(largestSize));
            }
            
            double actualRatio = smallSizeAvg > 0 ? largeSizeAvg / smallSizeAvg : 0;
            double expectedRatio = getExpectedRatio(algorithm, smallestSize, largestSize);
            String scalingRating = "N/A";
            if (expectedRatio > 0 && actualRatio > 0) {
                double ratioDifference = Math.abs(actualRatio / expectedRatio - 1);
                if (ratioDifference < 0.2) {
                    scalingRating = "Matches theory";
                } else if (ratioDifference < 0.5) {
                    scalingRating = "Close to theory";
                } else if (actualRatio < expectedRatio) {
                    scalingRating = "Better than theory";
                } else {
                    scalingRating = "Worse than theory";
                }
            }
            
            // Add row to table
            final AlgorithmData row = new AlgorithmData(
                    algorithm, 
                    getComplexityClass(algorithm),
                    String.format("%.2f ms", overallAvg),
                    consistencyRating,
                    scalingRating
            );
            
            // Add to table
            Platform.runLater(() -> data.add(row));
        }
        
        // Update table on UI thread
        Platform.runLater(() -> resultTable.setItems(data));
    }
    
    private double calculateAverage(List<Long> values) {
        long sum = 0;
        for (Long value : values) {
            sum += value;
        }
        return (double) sum / values.size();
    }
    
    private double calculateStdDev(List<Long> values, double mean) {
        double sumSquaredDiff = 0;
        for (Long value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / values.size());
    }
    
    private String getComplexityClass(String algorithm) {
        switch (algorithm) {
            case "Bubble Sort":
            case "Selection Sort":
            case "Insertion Sort":
                return "O(n²)";
            case "Quick Sort":
            case "Merge Sort":
            case "Heap Sort":
                return "O(n log n)";
            case "Counting Sort":
            case "Radix Sort":
                return "O(n)";
            case "Linear Search":
                return "O(n)";
            case "Binary Search":
                return "O(log n)";
            default:
                return "-";
        }
    }
    
    private double getExpectedRatio(String algorithm, int smallSize, int largeSize) {
        double n1 = smallSize;
        double n2 = largeSize;
        
        switch (algorithm) {
            case "Bubble Sort":
            case "Selection Sort":
            case "Insertion Sort":
                // O(n²)
                return (n2 * n2) / (n1 * n1);
            case "Quick Sort":
            case "Merge Sort":
            case "Heap Sort":
                // O(n log n)
                return (n2 * Math.log(n2)) / (n1 * Math.log(n1));
            case "Counting Sort":
            case "Radix Sort":
            case "Linear Search":
                // O(n)
                return n2 / n1;
            case "Binary Search":
                // O(log n)
                return Math.log(n2) / Math.log(n1);
            default:
                return 0;
        }
    }
    
    public BorderPane getPane() {
        return mainPane;
    }
    
    // Shutdown the executor properly
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    /**
     * Data class for the results table
     */
    public static class AlgorithmData {
        private final SimpleStringProperty algorithm;
        private final SimpleStringProperty complexity;
        private final SimpleStringProperty avgTime;
        private final SimpleStringProperty consistency;
        private final SimpleStringProperty scaleFactor;
        
        public AlgorithmData(String algorithm, String complexity, String avgTime, 
                            String consistency, String scaleFactor) {
            this.algorithm = new SimpleStringProperty(algorithm);
            this.complexity = new SimpleStringProperty(complexity);
            this.avgTime = new SimpleStringProperty(avgTime);
            this.consistency = new SimpleStringProperty(consistency);
            this.scaleFactor = new SimpleStringProperty(scaleFactor);
        }
        
        public SimpleStringProperty algorithmProperty() { return algorithm; }
        public SimpleStringProperty complexityProperty() { return complexity; }
        public SimpleStringProperty avgTimeProperty() { return avgTime; }
        public SimpleStringProperty consistencyProperty() { return consistency; }
        public SimpleStringProperty scaleFactorProperty() { return scaleFactor; }
    }
} 