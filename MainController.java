/* File: MainController.java */
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;

public class MainController {
    public static final String MODERN_STYLE = """
            .root {
                -fx-background-color: #f5f5f5;
            }
            .button {
                -fx-background-color: #2196F3;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-padding: 10 20;
                -fx-background-radius: 5;
            }
            .button:hover {
                -fx-background-color: #1976D2;
            }
            .text-field, .text-area, .combo-box {
                -fx-background-radius: 5;
                -fx-border-radius: 5;
                -fx-border-color: #E0E0E0;
                -fx-border-width: 1;
                -fx-padding: 8;
            }
            .label {
                -fx-font-size: 14px;
            }
            #title-label {
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: #1565C0;
            }
            #result-panel {
                -fx-background-color: white;
                -fx-background-radius: 5;
                -fx-padding: 15;
                -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0, 0, 3);
            }
            /* Pseudocode panel styling */
            .pseudocode-panel {
                -fx-background-color: #434C5E;
                -fx-padding: 10;
                -fx-background-radius: 8;
            }
            .pseudocode-line {
                -fx-font-family: "Monospaced";
                -fx-font-size: 14px;
                -fx-text-fill: #D8DEE9;
                -fx-padding: 2 0 2 0;
            }
            .highlighted-line {
                -fx-background-color: #88C0D0;
                -fx-background-radius: 3;
                -fx-padding: 2 4 2 4;
            }
            """;

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
    private PseudocodePanel pseudocodePanel;  // New panel for pseudocode display
    private CheckBox stepByStepCheckBox;

    public MainController() {
        root = new BorderPane();
        root.setPadding(new Insets(20));
        buildUI();
    }

    public Pane getRoot() {
        return root;
    }

    private void buildUI() {
        // --- Top: Title ---
        Label titleLabel = new Label("Algorithm Visualizer");
        titleLabel.setId("title-label");
        HBox topBox = new HBox(titleLabel);
        topBox.setAlignment(Pos.CENTER);
        root.setTop(topBox);

        // --- Left: Input Panel ---
        VBox inputPanel = new VBox(15);
        inputPanel.getStyleClass().add("input-panel");
        inputPanel.setPadding(new Insets(20));
        inputPanel.setPrefWidth(300);
        inputPanel.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5; -fx-padding: 15;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);");

        // Array size
        Label arraySizeLabel = new Label("Array Size:");
        arraySizeInput = new TextField();
        arraySizeInput.setPromptText("Enter size");

        // Array elements
        Label elementsLabel = new Label("Array Elements:");
        arrayElementsInput = new TextArea();
        arrayElementsInput.setPromptText("Elements will appear here");
        arrayElementsInput.setPrefRowCount(3);
        arrayElementsInput.setWrapText(true);

        // Algorithm selection
        Label algorithmLabel = new Label("Algorithm:");
        algorithmChoice = new ComboBox<>();
        algorithmChoice.getItems().addAll(
                "Selection Sort",
                "Insertion Sort",
                "Quick Sort",
                "Merge Sort",
                "Radix Sort",
                "Linear Search",
                "Binary Search"
        );
        algorithmChoice.setPromptText("Choose algorithm");

        // Target number input
        Label targetLabel = new Label("Search Target:");
        targetNumberInput = new TextField();
        targetNumberInput.setPromptText("Enter number to search");
        targetNumberInput.setDisable(true);

        // Enable target input for search algorithms
        algorithmChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.contains("Search")) {
                targetNumberInput.setDisable(false);
                targetNumberInput.setStyle("-fx-opacity: 1");
            } else {
                targetNumberInput.setDisable(true);
                targetNumberInput.setStyle("-fx-opacity: 0.5");
            }
        });

        // Step-by-step visualization toggle
        stepByStepCheckBox = new CheckBox("Step-by-Step Visualization");
        stepByStepCheckBox.setSelected(false);

        inputPanel.getChildren().addAll(
                arraySizeLabel, arraySizeInput,
                elementsLabel, arrayElementsInput,
                algorithmLabel, algorithmChoice,
                targetLabel, targetNumberInput,
                stepByStepCheckBox
        );
        root.setLeft(inputPanel);

        // --- Center: SplitPane for Visualizer and Pseudocode Panels ---
        visualizerPanel = new VisualizerPanel();
        pseudocodePanel = new PseudocodePanel();
        // Update pseudocode based on selected algorithm
        algorithmChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Set different pseudocode for different algorithms.
                switch (newVal) {
                    case "Insertion Sort":
                        pseudocodePanel.setPseudocode(new String[] {
                                "for i = 1 to n-1",
                                "   key = array[i]",
                                "   j = i - 1",
                                "   while j >= 0 and array[j] > key",
                                "       array[j+1] = array[j]",
                                "       j = j - 1",
                                "   array[j+1] = key"
                        });
                        break;
                    case "Selection Sort":
                        pseudocodePanel.setPseudocode(new String[] {
                                "for i = 0 to n-2",
                                "   minIndex = i",
                                "   for j = i+1 to n-1",
                                "       if array[j] < array[minIndex]",
                                "           minIndex = j",
                                "   swap(array[i], array[minIndex])"
                        });
                        break;
                    case "Quick Sort":
                        pseudocodePanel.setPseudocode(new String[] {
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
                        });
                        break;
                    case "Merge Sort":
                        pseudocodePanel.setPseudocode(new String[] {
                                "mergeSort(arr, l, r):",
                                "   if l < r:",
                                "       m = (l + r) / 2",
                                "       mergeSort(arr, l, m)",
                                "       mergeSort(arr, m+1, r)",
                                "       merge(arr, l, m, r)",
                                "",
                                "merge(arr, l, m, r):",
                                "   n1 = m - l + 1",
                                "   n2 = r - m",
                                "   create arrays L[0..n1-1] and R[0..n2-1]",
                                "   copy arr[l..m] into L[]",
                                "   copy arr[m+1..r] into R[]",
                                "   i = 0, j = 0, k = l",
                                "   while i < n1 and j < n2:",
                                "       if L[i] <= R[j]:",
                                "           arr[k] = L[i]",
                                "           i++",
                                "       else:",
                                "           arr[k] = R[j]",
                                "           j++",
                                "       k++",
                                "   copy remaining elements of L (if any)",
                                "   copy remaining elements of R (if any)"
                        });
                        break;
                    case "Radix Sort":
                        pseudocodePanel.setPseudocode(new String[] {
                                "radixSort(arr):",
                                "   m = getMax(arr)",
                                "   for exp = 1; m/exp > 0; exp = exp * 10:",
                                "       countSort(arr, exp)",
                                "",
                                "countSort(arr, exp):",
                                "   create output array",
                                "   create count[0..9] and initialize to 0",
                                "   for each element in arr:",
                                "       count[(element/exp) % 10]++",
                                "   for i = 1 to 9:",
                                "       count[i] += count[i-1]",
                                "   for i from n-1 downto 0:",
                                "       output[count[(arr[i]/exp)%10] - 1] = arr[i]",
                                "       count[(arr[i]/exp)%10]--",
                                "   copy output to arr"
                        });
                        break;
                    case "Linear Search":
                        pseudocodePanel.setPseudocode(new String[] {
                                "linearSearch(arr, target):",
                                "   for i = 0 to n-1:",
                                "       if arr[i] == target:",
                                "           return i",
                                "   return -1"
                        });
                        break;
                    case "Binary Search":
                        pseudocodePanel.setPseudocode(new String[] {
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
                        });
                        break;
                    default:
                        pseudocodePanel.setPseudocode(new String[] {
                                "// Pseudocode not available for this algorithm."
                        });
                        break;
                }
            }
        });


        SplitPane centerSplit = new SplitPane();
        centerSplit.getItems().addAll(visualizerPanel.getPane(), pseudocodePanel.getPane());
        centerSplit.setDividerPositions(0.6); // 60% visualizer, 40% pseudocode
        root.setCenter(centerSplit);

        // --- Bottom: Buttons, Progress, and Result Panel ---
        VBox bottomPanel = new VBox(10);
        bottomPanel.setPadding(new Insets(10));

        HBox buttonContainer = new HBox(15);
        buttonContainer.setAlignment(Pos.CENTER);

        generateRandomArrayButton = new Button("Generate Random Array");
        generateRandomArrayButton.setOnAction(e -> generateRandomArray());

        executeButton = new Button("Execute Algorithm");
        executeButton.setDefaultButton(true);
        executeButton.setOnAction(e -> executeAlgorithm());

        buttonContainer.getChildren().addAll(generateRandomArrayButton, executeButton);

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setVisible(false);

        // --- Result Panel with Modern Styling ---
        VBox resultPanel = new VBox(15);
        resultPanel.setId("result-panel");
        resultPanel.setPadding(new Insets(20));
        resultPanel.setMaxWidth(800);
        resultPanel.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #1A237E, #311B92); " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 10), innershadow(gaussian, rgba(255,255,255,0.2), 1, 1, 1, 0); " +
                        "-fx-border-color: rgba(255,255,255,0.1); " +
                        "-fx-border-width: 1; " +
                        "-fx-border-radius: 15;"
        );

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label("⚡");
        iconLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        iconLabel.setTextFill(Color.web("#FFD700"));
        Label resultTitle = new Label("ALGORITHM RESULTS");
        resultTitle.setFont(Font.font("System", FontWeight.BOLD, 22));
        resultTitle.setTextFill(Color.web("#FFFFFF"));
        resultTitle.setStyle("-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.3), 10, 0, 0, 0);");
        headerBox.getChildren().addAll(iconLabel, resultTitle);

        Region separator = new Region();
        separator.setPrefHeight(2);
        separator.setStyle(
                "-fx-background-color: linear-gradient(to right, transparent, #FFD700, transparent); " +
                        "-fx-opacity: 0.7;"
        );

        resultLabel = new Label("Awaiting algorithm execution...");
        resultLabel.setFont(Font.font("System", 16));
        resultLabel.setTextFill(Color.web("#E0E0E0"));
        resultLabel.setWrapText(true);
        resultLabel.setPadding(new Insets(10, 0, 0, 0));
        resultLabel.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 1);");

        resultPanel.getChildren().addAll(headerBox, separator, resultLabel);

        ScrollPane resultScrollPane = new ScrollPane(resultPanel);
        resultScrollPane.setFitToWidth(true);
        resultScrollPane.setPrefViewportHeight(200);
        resultScrollPane.setStyle(
                "-fx-background: transparent; " +
                        "-fx-background-color: transparent; " +
                        "-fx-padding: 10; " +
                        "-fx-border-color: transparent;"
        );

        bottomPanel.getChildren().addAll(buttonContainer, progressBar, resultScrollPane);
        bottomPanel.setAlignment(Pos.CENTER);
        root.setBottom(bottomPanel);
    }

    private void generateRandomArray() {
        try {
            int size = Integer.parseInt(arraySizeInput.getText());
            int[] arr = new int[size];
            for (int i = 0; i < size; i++) {
                arr[i] = (int) (Math.random() * 100);
            }
            String arrStr = Arrays.toString(arr)
                    .replace("[", "")
                    .replace("]", "")
                    .replace(",", "");
            arrayElementsInput.setText(arrStr);
        } catch (NumberFormatException ex) {
            showError("Please enter a valid array size");
        }
    }

    private void executeAlgorithm() {
        try {
            int size = Integer.parseInt(arraySizeInput.getText());
            String[] elements = arrayElementsInput.getText().split("\\s+");
            if (elements.length == 1 && elements[0].isEmpty()) {
                showError("Please enter array elements or generate a random array");
                return;
            }
            int[] arr = new int[elements.length];
            for (int i = 0; i < elements.length; i++) {
                arr[i] = Integer.parseInt(elements[i]);
            }
            if (arr.length != size) {
                showError("Number of elements doesn't match the specified array size");
                return;
            }
            String algorithm = algorithmChoice.getValue();
            if (algorithm == null || algorithm.isEmpty()) {
                showError("Please select an algorithm");
                return;
            }
            int target = 0;
            if (algorithm.contains("Search")) {
                try {
                    target = Integer.parseInt(targetNumberInput.getText());
                } catch (NumberFormatException ex) {
                    showError("Please enter a valid search target");
                    return;
                }
            }
            boolean stepByStep = stepByStepCheckBox.isSelected();
            // Disable controls during execution
            executeButton.setDisable(true);
            generateRandomArrayButton.setDisable(true);
            progressBar.setProgress(0);
            progressBar.setVisible(true);
            visualizerPanel.clear();

            // Run the algorithm on a background thread
            int finalTarget = target;
            Task<String> task = new Task<>() {
                @Override
                protected String call() throws Exception {
                    return AlgorithmExecutor.executeAlgorithm(arr, algorithm, finalTarget, stepByStep, visualizerPanel, this);
                }
            };

            task.setOnSucceeded(e -> {
                String result = task.getValue();
                FadeTransition fade = new FadeTransition(Duration.millis(300), resultLabel);
                fade.setFromValue(0);
                fade.setToValue(1);
                resultLabel.setText(result);
                fade.play();
                executeButton.setDisable(false);
                generateRandomArrayButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                progressBar.setVisible(false);
            });

            task.setOnFailed(e -> {
                showError("An error occurred: " + task.getException().getMessage());
                executeButton.setDisable(false);
                generateRandomArrayButton.setDisable(false);
                progressBar.progressProperty().unbind();
                progressBar.setProgress(0);
                progressBar.setVisible(false);
            });

            progressBar.progressProperty().bind(task.progressProperty());
            new Thread(task).start();
        } catch (NumberFormatException ex) {
            showError("Please enter valid numbers");
        } catch (Exception ex) {
            showError("An error occurred: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // Entry point for testing
    public static class MainApp extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            MainController controller = new MainController();
            Scene scene = new Scene(controller.getRoot(), 900, 700);
            scene.getStylesheets().add("data:text/css," + MODERN_STYLE.replaceAll("\n", ""));
            primaryStage.setScene(scene);
            primaryStage.setTitle("Algorithm Visualizer");
            primaryStage.show();
        }

        public static void main(String[] args) {
            launch(args);
        }
    }
}
