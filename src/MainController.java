import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import java.util.*;
import java.util.Arrays;

public class MainController {

    // UI Components
    private BorderPane root;
    private TextField arraySizeInput;
    private TextArea arrayElementsInput;
    private ComboBox<String> algorithmChoice;
    private TextField targetNumberInput;
    private Label resultLabel;
    private ProgressBar progressBar;
    private Button executeButton;
    private Button generateRandomArrayButton;
    private VisualizerPanel visualizerPanel;
    private PseudocodePanel pseudocodePanel;
    private CheckBox stepByStepCheckBox;
    private AlgorithmComplexityVisualizer complexityVisualizer;
    
    // Animation control buttons
    private Button playButton;
    private Button pauseButton;
    private Button stepButton;
    private Button resetButton;
    private HBox animationControlsBox;

    // CSS Style Classes
    private static final String TITLE_CLASS = "title";
    private static final String INPUT_PANEL_CLASS = "input-panel";
    private static final String CONTROL_LABEL_CLASS = "control-label";
    private static final String BUTTON_PRIMARY_CLASS = "button-primary";
    private static final String BUTTON_SECONDARY_CLASS = "button-secondary";
    private static final String FIELD_CLASS = "field";
    private static final String RESULT_PANEL_CLASS = "result-panel";
    private static final String RESULT_TEXT_CLASS = "result-text";
    private static final String HEADER_CLASS = "section-header";

    public MainController() {
        root = new BorderPane();
        root.getStyleClass().add("root-container");
        buildUI();
        applyStyles();
    }

    public Pane getRoot() {
        return root;
    }

    private void buildUI() {
        setupTopSection();
        setupLeftPanel();
        setupCenterPanel();
        setupBottomPanel();
    }

    private void setupTopSection() {
        Label titleLabel = new Label("Algorithm Visualizer");
        titleLabel.getStyleClass().add(TITLE_CLASS);

        HBox topBox = new HBox(titleLabel);
        topBox.setAlignment(Pos.CENTER);
        topBox.getStyleClass().add("top-container");
        topBox.setPadding(new Insets(20, 0, 20, 0));

        root.setTop(topBox);
    }

    private void setupLeftPanel() {
        VBox inputPanel = new VBox(15);
        inputPanel.getStyleClass().add(INPUT_PANEL_CLASS);
        inputPanel.setPadding(new Insets(20));
        inputPanel.setPrefWidth(300);

        // Array size control
        Label arraySizeLabel = createControlLabel("Array Size");
        arraySizeInput = new TextField();
        arraySizeInput.getStyleClass().add(FIELD_CLASS);
        arraySizeInput.setPromptText("Enter size");

        // Array elements control
        Label elementsLabel = createControlLabel("Array Elements");
        arrayElementsInput = new TextArea();
        arrayElementsInput.getStyleClass().add(FIELD_CLASS);
        arrayElementsInput.setPromptText("Elements will appear here");
        arrayElementsInput.setPrefRowCount(3);
        arrayElementsInput.setWrapText(true);

        // Algorithm selection
        Label algorithmLabel = createControlLabel("Select Algorithm");
        algorithmChoice = new ComboBox<>();
        algorithmChoice.getStyleClass().add(FIELD_CLASS);
        algorithmChoice.getItems().addAll(
                "Selection Sort",
                "Insertion Sort",
                "Bubble Sort",
                "Quick Sort",
                "Merge Sort",
                "Heap Sort",
                "Radix Sort",
                "Counting Sort",
                "Linear Search",
                "Binary Search"
        );
        algorithmChoice.setPromptText("Choose algorithm");
        algorithmChoice.setMaxWidth(Double.MAX_VALUE);

        // Target number for search algorithms
        Label targetLabel = createControlLabel("Search Target");
        targetNumberInput = new TextField();
        targetNumberInput.getStyleClass().addAll(FIELD_CLASS, "search-target");
        targetNumberInput.setPromptText("Enter number to search");
        targetNumberInput.setDisable(true);

        // Set up target input activation logic
        algorithmChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isSearchAlgorithm = newVal != null && newVal.contains("Search");
            targetNumberInput.setDisable(!isSearchAlgorithm);
            updatePseudocodePanel(newVal);
        });

        // Step-by-step visualization checkbox
        stepByStepCheckBox = new CheckBox("Step-by-Step Visualization");
        stepByStepCheckBox.getStyleClass().add("step-checkbox");

        // Add all controls to the panel
        inputPanel.getChildren().addAll(
                createSectionHeader("Input Configuration"),
                arraySizeLabel, arraySizeInput,
                elementsLabel, arrayElementsInput,
                algorithmLabel, algorithmChoice,
                targetLabel, targetNumberInput,
                stepByStepCheckBox
        );

        root.setLeft(inputPanel);
    }

    private Label createControlLabel(String text) {
        Label label = new Label(text + ":");
        label.getStyleClass().add(CONTROL_LABEL_CLASS);
        return label;
    }

    private Label createSectionHeader(String text) {
        Label header = new Label(text);
        header.getStyleClass().add(HEADER_CLASS);
        return header;
    }

    private void setupCenterPanel() {
        // Create a slider for the VisualizerPanel
        Slider animationSpeedSlider = new Slider(0.1, 2.0, 1.0);
        animationSpeedSlider.setShowTickLabels(true);
        animationSpeedSlider.setShowTickMarks(true);

        // Pass the slider to the constructor
        visualizerPanel = new AdaptiveVisualizerPanel(animationSpeedSlider);
        pseudocodePanel = new PseudocodePanel();

        // Link the panels (if needed)
        visualizerPanel.setPseudocodePanel(pseudocodePanel);

        // Create visualization content using a SplitPane
        SplitPane visualizeSplit = new SplitPane();
        visualizeSplit.getStyleClass().add("center-split-pane");
        visualizeSplit.getItems().addAll(visualizerPanel.getPane(), pseudocodePanel.getPane());
        visualizeSplit.setDividerPositions(0.6);

        // Create Algorithm Complexity Visualizer
        complexityVisualizer = new AlgorithmComplexityVisualizer();

        // Create TabPane to hold both Visualization and Complexity Analysis tabs
        TabPane tabPane = new TabPane();
        Tab visualizeTab = new Tab("Algorithm Visualization", visualizeSplit);
        Tab complexityTab = new Tab("Complexity Analysis", complexityVisualizer.getPane());
        visualizeTab.setClosable(false);
        complexityTab.setClosable(false);
        tabPane.getTabs().addAll(visualizeTab, complexityTab);

        root.setCenter(tabPane);
    }

    private void setupBottomPanel() {
        VBox bottomPanel = new VBox(15);
        bottomPanel.setPadding(new Insets(20));
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.getStyleClass().add("bottom-panel");

        // Button container
        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        generateRandomArrayButton = new Button("Generate Random Array");
        generateRandomArrayButton.getStyleClass().add(BUTTON_SECONDARY_CLASS);
        generateRandomArrayButton.setOnAction(e -> generateRandomArray());

        executeButton = new Button("Execute Algorithm");
        executeButton.getStyleClass().add(BUTTON_PRIMARY_CLASS);
        executeButton.setDefaultButton(true);
        executeButton.setOnAction(e -> executeAlgorithm());

        buttonContainer.getChildren().addAll(generateRandomArrayButton, executeButton);
        
        // Animation control buttons
        setupAnimationControls();

        // Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.getStyleClass().add("algorithm-progress");
        progressBar.setVisible(false);

        // Results panel
        VBox resultPanel = createResultPanel();

        bottomPanel.getChildren().addAll(buttonContainer, animationControlsBox, progressBar, resultPanel);
        root.setBottom(bottomPanel);
    }
    
    /**
     * Creates the animation control buttons (Play, Pause, Step, Reset).
     */
    private void setupAnimationControls() {
        animationControlsBox = new HBox(15);
        animationControlsBox.setAlignment(Pos.CENTER);
        animationControlsBox.setPadding(new Insets(10));
        animationControlsBox.getStyleClass().add("animation-controls");
        
        // Play button with icon
        playButton = new Button("▶ Play");
        playButton.getStyleClass().add("animation-button");
        playButton.setOnAction(e -> visualizerPanel.play());
        playButton.setDisable(true);
        
        // Pause button with icon
        pauseButton = new Button("⏸ Pause");
        pauseButton.getStyleClass().add("animation-button");
        pauseButton.setOnAction(e -> visualizerPanel.pause());
        pauseButton.setDisable(true);
        
        // Step button with icon
        stepButton = new Button("⏭ Step");
        stepButton.getStyleClass().add("animation-button");
        stepButton.setOnAction(e -> visualizerPanel.step());
        stepButton.setDisable(true);
        
        // Reset button with icon
        resetButton = new Button("🔄 Reset");
        resetButton.getStyleClass().add("animation-button");
        resetButton.setOnAction(e -> visualizerPanel.reset());
        resetButton.setDisable(true);
        
        // Add a vertical separator
        Separator separator = new Separator(Orientation.VERTICAL);
        separator.setPadding(new Insets(0, 10, 0, 10));
        
        // Add speed control slider
        Label speedLabel = new Label("Speed:");
        speedLabel.getStyleClass().add("control-label");
        
        Slider speedSlider = new Slider(0.1, 2.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setMinorTickCount(4);
        speedSlider.setPrefWidth(120);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (visualizerPanel != null) {
                visualizerPanel.setAnimationSpeed(newVal.doubleValue());
            }
        });
        
        // Labels for slider
        Label slowLabel = new Label("Slow");
        slowLabel.getStyleClass().add("slider-label");
        Label fastLabel = new Label("Fast");
        fastLabel.getStyleClass().add("slider-label");
        
        // Add tooltip for each button
        playButton.setTooltip(new Tooltip("Continue animation playback"));
        pauseButton.setTooltip(new Tooltip("Pause animation"));
        stepButton.setTooltip(new Tooltip("Execute one step at a time"));
        resetButton.setTooltip(new Tooltip("Reset animation to start"));
        
        // Status indicator dot
        Region statusDot = new Region();
        statusDot.setPrefSize(12, 12);
        statusDot.getStyleClass().add("status-dot");
        statusDot.setStyle("-fx-background-color: #38a169; -fx-background-radius: 6;");
        
        Label statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");
        
        HBox statusBox = new HBox(5, statusDot, statusLabel);
        statusBox.setAlignment(Pos.CENTER);
        
        animationControlsBox.getChildren().addAll(
            playButton, pauseButton, stepButton, resetButton,
            separator,
            speedLabel, slowLabel, speedSlider, fastLabel,
            new Separator(Orientation.VERTICAL),
            statusBox
        );
    }

    private VBox createResultPanel() {
        VBox resultPanel = new VBox(10);
        resultPanel.getStyleClass().add(RESULT_PANEL_CLASS);
        resultPanel.setPadding(new Insets(20));
        resultPanel.setMaxWidth(800);

        // Result header with icon
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("⚡");
        iconLabel.getStyleClass().add("result-icon");

        Label resultTitle = new Label("ALGORITHM RESULTS");
        resultTitle.getStyleClass().add("result-title");

        headerBox.getChildren().addAll(iconLabel, resultTitle);

        // Separator
        Region separator = new Region();
        separator.setPrefHeight(2);
        separator.getStyleClass().add("result-separator");

        // Result text
        resultLabel = new Label("Awaiting algorithm execution...");
        resultLabel.getStyleClass().add(RESULT_TEXT_CLASS);
        resultLabel.setWrapText(true);

        resultPanel.getChildren().addAll(headerBox, separator, resultLabel);

        // Wrap in scroll pane for longer results
        ScrollPane resultScrollPane = new ScrollPane(resultPanel);
        resultScrollPane.setFitToWidth(true);
        resultScrollPane.setPrefViewportHeight(200);
        resultScrollPane.getStyleClass().add("result-scroll-pane");

        return resultPanel;
    }

    private void updatePseudocodePanel(String algorithm) {
        if (algorithm == null) return;

        switch (algorithm) {
            case "Insertion Sort":
                updateInsertionSortPseudocode();
                break;
            case "Selection Sort":
                updateSelectionSortPseudocode();
                break;
            case "Bubble Sort":
                updateBubbleSortPseudocode();
                break;
            case "Quick Sort":
                updateQuickSortPseudocode();
                break;
            case "Merge Sort":
                updateMergeSortPseudocode();
                break;
            case "Heap Sort":
                updateHeapSortPseudocode();
                break;
            case "Radix Sort":
                updateRadixSortPseudocode();
                break;
            case "Counting Sort":
                updateCountingSortPseudocode();
                break;
            case "Linear Search":
                updateLinearSearchPseudocode();
                break;
            case "Binary Search":
                updateBinarySearchPseudocode();
                break;
            default:
                pseudocodePanel.setPseudocode(
                        new String[] {"// Pseudocode not available for this algorithm."},
                        new String[] {"// No pseudocode explanation available for this algorithm yet."}
                );
        }
    }

    private void updateInsertionSortPseudocode() {
        pseudocodePanel.setPseudocode(
                new String[] {
                        "for i = 1 to n-1",
                        "   key = array[i]",
                        "   j = i - 1",
                        "   while j >= 0 and array[j] > key",
                        "       array[j+1] = array[j]",
                        "       j = j - 1",
                        "   array[j+1] = key"
                },
                new String[] {
                        "// Start a loop that iterates through the array from the second element (index 1) to the last element.",
                        "// Take the current element and store it in 'key'.",
                        "// Initialize 'j' to point to the element before the current element.",
                        "// Compare key with each element on the left until a smaller element is found.",
                        "// Shift elements that are greater than key to the right.",
                        "// Move to the previous element to continue comparison.",
                        "// Insert the key at the correct position in the sorted sequence."
                }
        );
    }

    private void updateSelectionSortPseudocode() {
        pseudocodePanel.setPseudocode(
                new String[] {
                        "for i = 0 to n-2",
                        "   minIndex = i",
                        "   for j = i+1 to n-1",
                        "       if array[j] < array[minIndex]",
                        "           minIndex = j",
                        "   swap(array[i], array[minIndex])"
                },
                new String[] {
                        "// Loop through the array from the first element to the second-to-last.",
                        "// Assume the current index is the minimum element in the unsorted part.",
                        "// Search the unsorted part for the minimum element.",
                        "// If a smaller element is found, update minIndex.",
                        "// Update the index of the minimum element.",
                        "// Swap the found minimum with the first element of the unsorted part."
                }
        );
    }

    private void updateBubbleSortPseudocode() {
        pseudocodePanel.setPseudocode(
                new String[] {
                        "bubbleSort(array):",
                        "   for i = 0 to n-2:",
                        "       for j = 0 to n-i-2:",
                        "           if array[j] > array[j+1]:",
                        "               swap(array[j], array[j+1])",
                        "           // Optimization: track if any swaps occurred",
                        "           // If no swaps, array is sorted - early exit"
                },
                new String[] {
                        "// Main function for Bubble Sort.",
                        "// Outer loop controls the number of passes through the array.",
                        "// Inner loop compares adjacent elements.",
                        "// Compare if elements are in wrong order (ascending).",
                        "// Swap elements if they are in wrong order.",
                        "// An optimized version tracks if any swaps were made.",
                        "// This allows early termination if array becomes sorted."
                }
        );
    }

    private void updateQuickSortPseudocode() {
        pseudocodePanel.setPseudocode(
                new String[] {
                        "quickSort(arr, low, high):",
                        "   if low < high:",
                        "       pivotIndex = partition(arr, low, high)",
                        "       quickSort(arr, low, pivotIndex - 1)",
                        "       quickSort(arr, pivotIndex + 1, high)",
                        "",
                        "partition(arr, low, high):",
                        "   pivot = arr[high]",
                        "   i = low - 1",
                        "   for j = low to high - 1:",
                        "       if arr[j] < pivot:",
                        "           i++",
                        "           swap(arr[i], arr[j])",
                        "   swap(arr[i+1], arr[high])",
                        "   return i + 1"
                },
                new String[] {
                        "// Main recursive function with low and high indices of the subarray.",
                        "// Base case: If the subarray has at least 2 elements.",
                        "// Partition the array and get pivot's final position.",
                        "// Recursively sort elements before the pivot.",
                        "// Recursively sort elements after the pivot.",
                        "",
                        "// Function to place pivot at its correct position.",
                        "// Select the last element as the pivot.",
                        "// Index of smaller element (divider position).",
                        "// Check each element against the pivot.",
                        "// If current element is smaller than pivot.",
                        "// Increment the smaller element index.",
                        "// Swap current element with element at i.",
                        "// Place the pivot in its final sorted position.",
                        "// Return the pivot's index."
                }
        );
    }

    private void updateMergeSortPseudocode() {
        pseudocodePanel.setPseudocode(
                new String[] {
                        "mergeSort(arr, l, r):",
                        "   if l < r:",
                        "       m = (l + r) / 2",
                        "       mergeSort(arr, l, m)",
                        "       mergeSort(arr, m+1, r)",
                        "       merge(arr, l, m, r)",
                        "",
                        "merge(arr, l, m, r):",
                        "   create arrays L[0..n1-1] and R[0..n2-1]",
                        "   copy arr[l..m] into L[]",
                        "   copy arr[m+1..r] into R[]",
                        "   i = 0, j = 0, k = l",
                        "   while i < n1 and j < n2:",
                        "       if L[i] <= R[j]:",
                        "           arr[k++] = L[i++]",
                        "       else:",
                        "           arr[k++] = R[j++]",
                        "   copy remaining elements of L and R if any"
                },
                new String[] {
                        "// Main function to sort arr[l..r].",
                        "// Check if further division is needed.",
                        "// Find the middle point for division.",
                        "// Sort first half.",
                        "// Sort second half.",
                        "// Merge the sorted halves.",
                        "",
                        "// Function to merge two sorted subarrays.",
                        "// Create temporary arrays for left and right parts.",
                        "// Copy data to temporary arrays.",
                        "// Copy data to temporary arrays.",
                        "// Initial indices for merging.",
                        "// Merge temp arrays back into arr[l..r].",
                        "// Choose smaller element from L or R.",
                        "// Copy the element and advance indices.",
                        "// If R element is smaller.",
                        "// Copy the element and advance indices.",
                        "// Copy any remaining elements from either array."
                }
        );
    }

    private void updateHeapSortPseudocode() {
        pseudocodePanel.setPseudocode(
                new String[] {
                        "heapSort(array):",
                        "   buildMaxHeap(array)",
                        "   for i = n-1 down to 1:",
                        "       swap(array[0], array[i])",
                        "       heapify(array, i, 0)",
                        "",
                        "heapify(array, n, i):",
                        "   largest = i",
                        "   left = 2*i + 1",
                        "   right = 2*i + 2",
                        "",
                        "   if left < n and array[left] > array[largest]:",
                        "       largest = left",
                        "",
                        "   if right < n and array[right] > array[largest]:",
                        "       largest = right",
                        "",
                        "   if largest != i:",
                        "       swap(array[i], array[largest])",
                        "       heapify(array, n, largest)"
                },
                new String[] {
                        "// Main function for Heap Sort.",
                        "// First build a max heap (all parents >= children).",
                        "// Then extract elements one by one from the heap.",
                        "// Move the current root (maximum) to the end.",
                        "// Call heapify on the reduced heap.",
                        "",
                        "// Function to heapify a subtree rooted at node i.",
                        "// Initialize largest as root.",
                        "// Calculate left child index.",
                        "// Calculate right child index.",
                        "",
                        "// If left child is larger than root.",
                        "// Update largest to be the left child.",
                        "",
                        "// If right child is larger than current largest.",
                        "// Update largest to be the right child.",
                        "",
                        "// If largest is not the root.",
                        "// Swap root with the largest element.",
                        "// Recursively heapify the affected subtree."
                }
        );
    }

    private void updateRadixSortPseudocode() {
        pseudocodePanel.setPseudocode(
                new String[] {
                        "radixSort(arr):",
                        "   m = getMax(arr)",
                        "   for exp = 1; m/exp > 0; exp *= 10:",
                        "       countSort(arr, exp)",
                        "",
                        "countSort(arr, exp):",
                        "   create output array and count array[0..9]",
                        "   for i = 0 to n-1:",
                        "       count[(arr[i]/exp) % 10]++",
                        "   for i = 1 to 9:",
                        "       count[i] += count[i-1]",
                        "   for i = n-1 downto 0:",
                        "       output[count[(arr[i]/exp)%10] - 1] = arr[i]",
                        "       count[(arr[i]/exp)%10]--",
                        "   copy output back to arr"
                },
                new String[] {
                        "// Main function for Radix Sort.",
                        "// Find the maximum number to determine digit count.",
                        "// Process each digit place (1s, 10s, 100s, etc).",
                        "// Use counting sort for the current digit place.",
                        "",
                        "// Sort array based on digits at position exp.",
                        "// Create auxiliary arrays.",
                        "// Count occurrences of each digit.",
                        "// Store count of each digit at the current place.",
                        "// Change count to contain actual positions.",
                        "// Accumulate counts for stable sorting.",
                        "// Build the output array in reverse order for stability.",
                        "// Place each element at its sorted position.",
                        "// Decrement count for the digit.",
                        "// Copy sorted array back to original array."
                }
        );
    }

    private void updateCountingSortPseudocode() {
        pseudocodePanel.setPseudocode(
                new String[] {
                        "countingSort(array):",
                        "   find min and max values in array",
                        "   create count array of size (max-min+1)",
                        "   initialize count array to zeros",
                        "   for each element i in array:",
                        "       count[array[i] - min]++",
                        "   for i = 1 to count.length-1:",
                        "       count[i] += count[i-1]",
                        "   create output array of same size as input",
                        "   for i = array.length-1 downto 0:",
                        "       output[count[array[i] - min] - 1] = array[i]",
                        "       count[array[i] - min]--",
                        "   copy output array back to original array"
                },
                new String[] {
                        "// Main function for Counting Sort.",
                        "// Find the range of values to determine count array size.",
                        "// Create a count array to store the count of each unique element.",
                        "// Initialize all counts to zero.",
                        "// Count occurrences of each element.",
                        "// Store count of each element in count array.",
                        "// Modify count array to contain position information.",
                        "// Each count now contains the position of the next occurrence.",
                        "// Create output array to store the sorted elements.",
                        "// Process input array in reverse for stability.",
                        "// Place elements in their sorted positions in output.",
                        "// Decrement count for this element.",
                        "// Transfer sorted elements back to original array."
                }
        );
    }

    private void updateLinearSearchPseudocode() {
        pseudocodePanel.setPseudocode(
                new String[] {
                        "linearSearch(arr, target):",
                        "   for i = 0 to n-1:",
                        "       if arr[i] == target:",
                        "           return i",
                        "   return -1"
                },
                new String[] {
                        "// Function to search for target in array.",
                        "// Check each element one by one.",
                        "// Compare current element with search target.",
                        "// Return index if element found.",
                        "// Return -1 if element not found in array."
                }
        );
    }

    private void updateBinarySearchPseudocode() {
        pseudocodePanel.setPseudocode(
                new String[] {
                        "binarySearch(arr, target):",
                        "   low = 0, high = n-1",
                        "   while low <= high:",
                        "       mid = (low + high) / 2",
                        "       if arr[mid] == target:",
                        "           return mid",
                        "       else if arr[mid] < target:",
                        "           low = mid + 1",
                        "       else:",
                        "           high = mid - 1",
                        "   return -1"
                },
                new String[] {
                        "// Function to perform binary search (requires sorted array).",
                        "// Initialize search range from start to end of array.",
                        "// Continue searching while valid range exists.",
                        "// Calculate middle index of current range.",
                        "// Check if middle element is the target.",
                        "// If found, return the index.",
                        "// If target is greater, ignore left half.",
                        "// Search in the right half (mid+1 to high).",
                        "// If target is smaller, ignore right half.",
                        "// Search in the left half (low to mid-1).",
                        "// Target not found in the array."
                }
        );
    }

    private void applyStyles() {
        root.getStylesheets().add(getClass().getResource("modern-visualizer.css").toExternalForm());
    }

    private void generateRandomArray() {
        try {
            int size = Integer.parseInt(arraySizeInput.getText().trim());
            if (size <= 0 || size > 1000) { // Added upper bound check
                showError("Array size must be a positive number (1-1000).");
                return;
            }

            int[] arr = new int[size];
            for (int i = 0; i < size; i++) {
                arr[i] = (int) (Math.random() * 100);
            }

            String arrStr = Arrays.toString(arr)
                    .replace("[", "")
                    .replace("]", "")
                    .replace(",", "");

            arrayElementsInput.setText(arrStr);

            // Animation feedback for generation
            FadeTransition fade = new FadeTransition(Duration.millis(300), arrayElementsInput);
            fade.setFromValue(0.7);
            fade.setToValue(1.0);
            fade.play();

        } catch (NumberFormatException ex) {
            showError("Please enter a valid array size (integer).");
        }
    }

    private void executeAlgorithm() {
        try {
            // Validate and parse inputs
            if (!validateInputs()) {
                return;
            }

            int size = parseArraySize();
            int[] arr = parseArrayElements(size);
            String algorithm = getSelectedAlgorithm();
            int target = getSearchTarget(algorithm);
            boolean stepByStep = stepByStepCheckBox.isSelected();

            // Disable UI controls during execution
            setUIExecutionState(true);

            // Display initial array
            visualizerPanel.clear();
            visualizerPanel.displayArray(arr);

            // Run algorithm in background
            runAlgorithmTask(arr, algorithm, target, stepByStep);

        } catch (Exception ex) {
            showError("An unexpected error occurred: " + ex.getMessage());
        }
    }

    private boolean validateInputs() {
        // Check if algorithm is selected
        if (algorithmChoice.getValue() == null) {
            showError("Please select an algorithm from the dropdown.");
            return false;
        }

        // Check array size
        if (arraySizeInput.getText().trim().isEmpty()) {
            showError("Please enter an array size.");
            return false;
        }

        // Check array elements
        if (arrayElementsInput.getText().trim().isEmpty()) {
            showError("Please enter array elements or generate a random array.");
            return false;
        }

        // Check search target for search algorithms
        if (algorithmChoice.getValue().contains("Search") &&
                targetNumberInput.getText().trim().isEmpty()) {
            showError("Please enter a search target value.");
            return false;
        }

        return true;
    }

    private int parseArraySize() {
        String sizeText = arraySizeInput.getText().trim();
        int size = Integer.parseInt(sizeText);
        if (size <= 0) {
            throw new IllegalArgumentException("Array size must be a positive integer.");
        }
        return size;
    }

    private int[] parseArrayElements(int expectedSize) {
        String elementsText = arrayElementsInput.getText().trim();
        String[] elements = elementsText.split("\\s+");

        if (elements.length != expectedSize) {
            throw new IllegalArgumentException("Number of elements does not match the specified array size.");
        }

        int[] arr = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            arr[i] = Integer.parseInt(elements[i]);
        }

        return arr;
    }

    private String getSelectedAlgorithm() {
        return algorithmChoice.getValue();
    }

    private int getSearchTarget(String algorithm) {
        if (algorithm.contains("Search")) {
            return Integer.parseInt(targetNumberInput.getText().trim());
        }
        return 0; // Not relevant for sorting algorithms
    }

    private void setUIExecutionState(boolean isExecuting) {
        executeButton.setDisable(isExecuting);
        generateRandomArrayButton.setDisable(isExecuting);
        progressBar.setVisible(isExecuting);
        
        // Update animation control buttons
        playButton.setDisable(!isExecuting);
        pauseButton.setDisable(!isExecuting);
        stepButton.setDisable(!isExecuting);
        resetButton.setDisable(!isExecuting);

        if (isExecuting) {
            progressBar.setProgress(0);
            resultLabel.setText("Algorithm executing...");
        } else {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            
            // Disable animation controls when execution completes
            playButton.setDisable(true);
            pauseButton.setDisable(true);
            stepButton.setDisable(true);
            resetButton.setDisable(true);
        }
    }

    private void runAlgorithmTask(int[] arr, String algorithm, int target, boolean stepByStep) {
        // Store the current state in the visualizer for reset functionality
        visualizerPanel.setCurrentState(arr, algorithm);
        
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                return AlgorithmExecutor.executeAlgorithm(arr, algorithm, target, stepByStep, visualizerPanel, this);
            }
        };

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            showResult(result);
            setUIExecutionState(false);
        });

        task.setOnFailed(e -> {
            showError("Algorithm execution failed: " + task.getException().getMessage());
            setUIExecutionState(false);
        });

        progressBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    private void showResult(String result) {
        FadeTransition fade = new FadeTransition(Duration.millis(400), resultLabel);
        fade.setFromValue(0.3);
        fade.setToValue(1.0);
        resultLabel.setText(result);
        fade.play();
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().getStyleClass().add("custom-alert");
            alert.showAndWait();
        });
    }

    /**
     * Shuts down resources used by the controller.
     * Should be called when the application is closing.
     */
    public void shutdown() {
        if (complexityVisualizer != null) {
            complexityVisualizer.shutdown();
        }
    }

    // Entry point for testing
    public static class MainApp extends Application {
        private MainController controller;
        
        @Override
        public void start(Stage primaryStage) {
            controller = new MainController();
            Scene scene = new Scene(controller.getRoot(), 1000, 700);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Algorithm Visualizer");
            primaryStage.show();
            
            // Clean up resources when application closes
            primaryStage.setOnCloseRequest(e -> {
                if (controller != null) {
                    controller.shutdown();
                }
            });
        }

        public static void main(String[] args) {
            launch(args);
        }
    }
}