import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Panel responsible for visualizing sorting and searching algorithms.
 * Displays array elements as blocks and animates operations performed on them.
 */
public class VisualizerPanel {
    // UI Components
    private final VBox mainContainer;
    private final FlowPane arrayContainer;
    private final Label statusLabel;
    private Slider speedSlider;
    private final List<StackPane> blockPanes;
    private PseudocodePanel pseudocodePanel;

    // Animation Control
    private boolean isAnimating = false;
    private final DoubleProperty animationSpeedMultiplier = new SimpleDoubleProperty(1.0);
    
    // Enhanced Animation Control
    private boolean isPaused = false;
    private boolean isStepMode = false;
    private boolean waitingForStep = false;
    private final Object stepLock = new Object();
    private int[] currentArray = null;
    private String currentAlgorithm = null;

    // Visual Constants
    public static final int BLOCK_WIDTH = 30;
    public static final int BLOCK_HEIGHT = 30;
    public static final int BLOCK_SPACING = 5;
    public static final int MAX_BLOCKS_PER_ROW = 15;

    // Color Scheme
    public static final Color DEFAULT_BLOCK_COLOR = Color.web("#60a5fa");  // Modern blue
    public static final Color COMPARING_COLOR = Color.web("#fbbf24");      // Amber
    public static final Color SORTED_COLOR = Color.web("#34d399");         // Emerald
    public static final Color SELECTED_COLOR = Color.web("#f97316");       // Orange
    public static final Color TEXT_COLOR = Color.web("#1f2937");           // Dark gray
    public static final Color BACKGROUND_COLOR = Color.web("#f8fafc");     // Light gray

    // Animation Constants
    private static final double BASE_ANIMATION_DURATION = 500;

    /**
     * Creates a new visualizer panel with all necessary components.
     */
    public VisualizerPanel(Slider speedSlider) {
        this.speedSlider = speedSlider;
        // Initialize main container
        mainContainer = new VBox(12);
        mainContainer.setPadding(new Insets(16));
        mainContainer.getStyleClass().add("visualizer-panel");

        // Create components
        VBox headerBox = createHeaderSection();
        HBox controlsBox = createControlsSection();
        arrayContainer = createArrayContainer();
        HBox legendBox = createLegend();
        statusLabel = createStatusLabel();

        // Assemble UI
        mainContainer.getChildren().addAll(
                headerBox,
                controlsBox,
                createSeparator(),
                arrayContainer,
                legendBox,
                statusLabel
        );

        // Initialize block container
        blockPanes = new ArrayList<>();

        // Apply styling
        applyModernStyling();
    }

    /**
     * Creates the header section with title and description.
     */
    private VBox createHeaderSection() {
        VBox headerBox = new VBox(6);
        headerBox.setAlignment(Pos.CENTER);

        Label header = new Label("Algorithm Visualization");
        header.getStyleClass().add("header-title");
        header.setFont(Font.font("System", FontWeight.BOLD, 18));

        Label description = new Label("Watch the algorithm execution step by step");
        description.getStyleClass().add("header-description");
        description.setFont(Font.font("System", FontPosture.ITALIC, 13));

        headerBox.getChildren().addAll(header, description);
        return headerBox;
    }

    /**
     * Creates the animation controls section.
     */
    private HBox createControlsSection() {
        HBox controlsBox = new HBox(12);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(8, 0, 12, 0));

        Label speedLabel = new Label("Animation Speed:");
        speedLabel.getStyleClass().add("control-label");

        speedSlider = new Slider(0.1, 2.0, 1.0);
        speedSlider.getStyleClass().add("speed-slider");
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(0.5);
        speedSlider.setMinorTickCount(4);
        speedSlider.setPrefWidth(200);

        // Add custom labels
        Label slowLabel = new Label("Slow");
        slowLabel.getStyleClass().add("slider-label");
        Label fastLabel = new Label("Fast");
        fastLabel.getStyleClass().add("slider-label");

        // Bind animation speed to slider value
        animationSpeedMultiplier.bind(speedSlider.valueProperty());

        controlsBox.getChildren().addAll(speedLabel, slowLabel, speedSlider, fastLabel);
        return controlsBox;
    }

    /**
     * Creates the container for array blocks.
     */
    private FlowPane createArrayContainer() {
        FlowPane container = new FlowPane();
        container.setHgap(BLOCK_SPACING);
        container.setVgap(BLOCK_SPACING);
        container.setPrefWrapLength((BLOCK_WIDTH + BLOCK_SPACING) * MAX_BLOCKS_PER_ROW);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(20, 0, 20, 0));
        container.getStyleClass().add("array-container");

        // Add border pane for visual emphasis
        Border border = new Border(new BorderStroke(
                Color.web("#e2e8f0"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(8),
                new BorderWidths(1)
        ));
        container.setBorder(border);

        // Set minimum height to prevent layout jump
        container.setMinHeight(BLOCK_HEIGHT * 3 + BLOCK_SPACING * 4 + 40);
        return container;
    }

    /**
     * Creates a legend explaining the color coding.
     */
    private HBox createLegend() {
        HBox legend = new HBox(15);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(8, 0, 12, 0));
        legend.getStyleClass().add("legend-container");

        legend.getChildren().addAll(
                createLegendItem("Unsorted", DEFAULT_BLOCK_COLOR),
                createLegendItem("Comparing", COMPARING_COLOR),
                createLegendItem("Selected", SELECTED_COLOR),
                createLegendItem("Sorted", SORTED_COLOR)
        );
        return legend;
    }

    /**
     * Creates a legend item with a colored box and label.
     */
    private HBox createLegendItem(String text, Color color) {
        Rectangle rect = new Rectangle(16, 16);
        rect.setFill(color);
        rect.setStroke(Color.web("#64748b"));
        rect.setArcHeight(4);
        rect.setArcWidth(4);
        rect.getStyleClass().add("legend-box");

        Label label = new Label(text);
        label.getStyleClass().add("legend-label");

        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER);
        item.getChildren().addAll(rect, label);
        return item;
    }

    /**
     * Creates the status label.
     */
    private Label createStatusLabel() {
        Label label = new Label("Algorithm not started");
        label.getStyleClass().add("status-label");
        label.setFont(Font.font("System", 14));
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(6, 0, 0, 0));
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    /**
     * Creates a visual separator.
     */
    private Region createSeparator() {
        Region separator = new Region();
        separator.setPrefHeight(1);
        separator.setMaxWidth(Double.MAX_VALUE);
        separator.getStyleClass().add("separator");
        return separator;
    }

    /**
     * Applies modern styling to the UI components.
     */
    private void applyModernStyling() {
        mainContainer.setStyle(
                "-fx-background-color: " + toHexString(BACKGROUND_COLOR) + "; " +
                        "-fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);"
        );

        statusLabel.setStyle(
                "-fx-text-fill: " + toHexString(TEXT_COLOR) + ";" +
                        "-fx-padding: 8px;"
        );
    }

    /**
     * Converts a JavaFX Color to a CSS hex string.
     */
    private String toHexString(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    /**
     * Gets the main container pane.
     */
    public VBox getPane() {
        return mainContainer;
    }

    /**
     * Displays an array as visual blocks in the container.
     * @param arr The array to visualize
     */
    public void displayArray(int[] arr) {
        Platform.runLater(() -> {
            arrayContainer.getChildren().clear();
            blockPanes.clear();

            // Create each block
            for (int value : arr) {
                StackPane block = createBlock(value);
                blockPanes.add(block);
            }

            arrayContainer.getChildren().addAll(blockPanes);

            // Apply entrance animation for initial array
            animateArrayEntrance();
        });
    }

    /**
     * Creates a visual block representing an array element.
     * @param value The value to display
     * @return A StackPane containing the block visualization
     */
    private StackPane createBlock(int value) {
        // Create the block rectangle
        Rectangle rect = new Rectangle(BLOCK_WIDTH, BLOCK_HEIGHT);
        rect.setArcWidth(6);
        rect.setArcHeight(6);
        rect.setFill(DEFAULT_BLOCK_COLOR);
        rect.setStroke(DEFAULT_BLOCK_COLOR.darker());
        rect.getStyleClass().add("array-block");

        // Create enhanced tooltip
        Tooltip tooltip = new Tooltip("Value: " + value);
        tooltip.setFont(Font.font("System", FontWeight.NORMAL, 12));
        tooltip.setShowDelay(Duration.millis(200));
        Tooltip.install(rect, tooltip);

        // Add hover effects
        rect.setOnMouseEntered(e -> {
            rect.setEffect(new DropShadow(10, Color.GRAY));
            rect.setFill(DEFAULT_BLOCK_COLOR.darker());
            rect.setScaleX(1.1);
            rect.setScaleY(1.1);
        });

        rect.setOnMouseExited(e -> {
            rect.setEffect(null);
            rect.setFill(DEFAULT_BLOCK_COLOR);
            rect.setScaleX(1.0);
            rect.setScaleY(1.0);
        });

        // Value label
        Label label = new Label(String.valueOf(value));
        label.setFont(Font.font("System", FontWeight.BOLD, 13));
        label.setTextFill(Color.WHITE);

        // Combine elements
        StackPane block = new StackPane(rect, label);
        return block;
    }

    /**
     * Animates the entrance of blocks when first displaying the array.
     */
    private void animateArrayEntrance() {
        for (int i = 0; i < blockPanes.size(); i++) {
            final int index = i;
            StackPane block = blockPanes.get(i);

            // Set initial state
            block.setOpacity(0);
            block.setTranslateY(20);
            block.setScaleX(0.8);
            block.setScaleY(0.8);

            // Create animation
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(block.opacityProperty(), 0),
                            new KeyValue(block.translateYProperty(), 20),
                            new KeyValue(block.scaleXProperty(), 0.8),
                            new KeyValue(block.scaleYProperty(), 0.8)
                    ),
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(block.opacityProperty(), 1),
                            new KeyValue(block.translateYProperty(), 0),
                            new KeyValue(block.scaleXProperty(), 1),
                            new KeyValue(block.scaleYProperty(), 1)
                    )
            );

            timeline.setDelay(Duration.millis(50 * index));
            timeline.play();
        }
    }

    /**
     * Updates the status message.
     * @param message The status message to display
     */
    public void updateStatus(String message) {
        Platform.runLater(() -> {
            // Apply a subtle fade transition for status updates
            FadeTransition fade = new FadeTransition(Duration.millis(150), statusLabel);
            fade.setFromValue(0.7);
            fade.setToValue(1.0);

            statusLabel.setText(message);
            fade.play();
        });
    }

    /**
     * Calculates the animation duration based on the speed slider.
     * @return Duration for animations
     */
    private Duration getAnimationDuration() {
        return Duration.millis(BASE_ANIMATION_DURATION / animationSpeedMultiplier.get());
    }

    /**
     * Animates swapping two blocks with enhanced visual effects.
     * @param i Index of first block
     * @param j Index of second block
     */
    public void animateSwap(int i, int j) {
        if (isAnimating || i < 0 || j < 0 || i >= blockPanes.size() || j >= blockPanes.size() || i == j) {
            return;
        }

        isAnimating = true;

        StackPane blockI = blockPanes.get(i);
        StackPane blockJ = blockPanes.get(j);

        // Calculate distance between blocks
        double distance = (BLOCK_WIDTH + BLOCK_SPACING) * Math.abs(j - i);
        Duration duration = getAnimationDuration();
        Duration halfDuration = duration.divide(2);

        // Create main translation transitions
        TranslateTransition translateI = new TranslateTransition(duration, blockI);
        translateI.setByX(j > i ? distance : -distance);

        TranslateTransition translateJ = new TranslateTransition(duration, blockJ);
        translateJ.setByX(i > j ? distance : -distance);

        // Create visual enhancement transitions

        // Scale transitions for emphasis
        ScaleTransition scaleUpI = new ScaleTransition(halfDuration, blockI);
        scaleUpI.setToX(1.2);
        scaleUpI.setToY(1.2);

        ScaleTransition scaleDownI = new ScaleTransition(halfDuration, blockI);
        scaleDownI.setToX(1.0);
        scaleDownI.setToY(1.0);

        ScaleTransition scaleUpJ = new ScaleTransition(halfDuration, blockJ);
        scaleUpJ.setToX(1.2);
        scaleUpJ.setToY(1.2);

        ScaleTransition scaleDownJ = new ScaleTransition(halfDuration, blockJ);
        scaleDownJ.setToX(1.0);
        scaleDownJ.setToY(1.0);

        // Highlight effect for blocks being swapped
        Rectangle rectI = (Rectangle) blockI.getChildren().get(0);
        Rectangle rectJ = (Rectangle) blockJ.getChildren().get(0);

        // Store original colors
        Color originalColorI = (Color) rectI.getFill();
        Color originalColorJ = (Color) rectJ.getFill();

        // Set highlight colors
        Platform.runLater(() -> {
            rectI.setFill(COMPARING_COLOR);
            rectJ.setFill(COMPARING_COLOR);

            // Add a subtle glow effect
            rectI.setEffect(new Glow(0.5));
            rectJ.setEffect(new Glow(0.5));
        });

        // Sequence the animations
        SequentialTransition seqI = new SequentialTransition(
                new ParallelTransition(scaleUpI),
                new ParallelTransition(scaleDownI)
        );

        SequentialTransition seqJ = new SequentialTransition(
                new ParallelTransition(scaleUpJ),
                new ParallelTransition(scaleDownJ)
        );

        // Create the full parallel animation
        ParallelTransition parallelTransition = new ParallelTransition(
                translateI, translateJ, seqI, seqJ
        );

        parallelTransition.setOnFinished(e -> {
            Platform.runLater(() -> {
                // Reset transitions and effects
                blockI.setTranslateX(0);
                blockJ.setTranslateX(0);
                rectI.setEffect(null);
                rectJ.setEffect(null);

                // Swap the blocks in the list
                StackPane temp = blockPanes.get(i);
                blockPanes.set(i, blockPanes.get(j));
                blockPanes.set(j, temp);

                // Refresh the display
                arrayContainer.getChildren().clear();
                arrayContainer.getChildren().addAll(blockPanes);

                // Reset animation flag
                isAnimating = false;
            });
        });

        parallelTransition.play();
    }

    /**
     * Animates shifting an element from one index to another.
     * @param fromIndex The source index
     * @param toIndex The destination index
     */
    public void animateShiftRight(int fromIndex, int toIndex) {
        if (isAnimating || fromIndex < 0 || toIndex < 0 ||
                fromIndex >= blockPanes.size() || toIndex >= blockPanes.size()) {
            return;
        }

        isAnimating = true;

        StackPane block = blockPanes.get(fromIndex);
        double shiftDistance = (BLOCK_WIDTH + BLOCK_SPACING) * (toIndex - fromIndex);
        Duration duration = getAnimationDuration().multiply(0.6);

        // Highlight the block being shifted
        Rectangle rect = (Rectangle) block.getChildren().get(0);
        Color originalColor = (Color) rect.getFill();
        Platform.runLater(() -> rect.setFill(SELECTED_COLOR));

        // Create translation animation
        TranslateTransition translate = new TranslateTransition(duration, block);
        translate.setByX(shiftDistance);

        // Create subtle bounce effect
        KeyValue kv1 = new KeyValue(block.translateYProperty(), -10, Interpolator.EASE_OUT);
        KeyValue kv2 = new KeyValue(block.translateYProperty(), 0, Interpolator.EASE_IN);
        KeyFrame kf1 = new KeyFrame(duration.divide(2), kv1);
        KeyFrame kf2 = new KeyFrame(duration, kv2);
        Timeline bounce = new Timeline(kf1, kf2);

        // Play animations in parallel
        ParallelTransition shiftAnimation = new ParallelTransition(translate, bounce);

        shiftAnimation.setOnFinished(e -> {
            block.setTranslateX(0);
            block.setTranslateY(0);

            Platform.runLater(() -> {
                // Update the data structure
                StackPane removedBlock = blockPanes.remove(fromIndex);
                blockPanes.add(toIndex, removedBlock);

                // Update the display
                arrayContainer.getChildren().clear();
                arrayContainer.getChildren().addAll(blockPanes);

                // Reset animation flag
                isAnimating = false;
            });
        });

        shiftAnimation.play();
    }

    /**
     * Updates the color of a specific block with a smooth transition.
     * @param index The index of the block to highlight
     * @param color The color to set
     */
    public void highlightBlock(int index, Color color) {
        if (index < 0 || index >= blockPanes.size()) return;

        Platform.runLater(() -> {
            StackPane block = blockPanes.get(index);
            Rectangle rect = (Rectangle) block.getChildren().get(0);

            // Create a color transition animation
            Color currentColor = (Color) rect.getFill();

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(rect.fillProperty(), currentColor)
                    ),
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(rect.fillProperty(), color)
                    )
            );

            // Add a subtle pulse effect
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), block);
            scaleUp.setToX(1.1);
            scaleUp.setToY(1.1);

            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), block);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);

            // Play animations sequentially
            SequentialTransition sequence = new SequentialTransition(
                    scaleUp, scaleDown
            );

            // Play color change and scale in parallel
            ParallelTransition parallel = new ParallelTransition(timeline, sequence);
            parallel.play();
        });
    }

    /**
     * Resets the color of a block to the default color.
     * @param index The index of the block
     */
    public void resetBlockColor(int index) {
        if (index < 0 || index >= blockPanes.size()) return;
        highlightBlock(index, DEFAULT_BLOCK_COLOR);
    }

    /**
     * Clears the visualizer panel.
     */
    public void clear() {
        Platform.runLater(() -> {
            arrayContainer.getChildren().clear();
            blockPanes.clear();
            statusLabel.setText("Algorithm not started.");
        });
    }

    /**
     * Highlights a specific pseudocode line in the associated pseudocode panel.
     * @param lineNumber The line number to highlight
     */
    public void highlightPseudocodeLine(int lineNumber) {
        if (pseudocodePanel != null) {
            Platform.runLater(() -> pseudocodePanel.highlightLine(lineNumber));
        }
    }

    /**
     * Gets the associated pseudocode panel.
     * @return The pseudocode panel
     */
    public PseudocodePanel getPseudocodePanel() {
        return pseudocodePanel;
    }

    /**
     * Sets the associated pseudocode panel for line highlighting.
     * @param pseudocodePanel The pseudocode panel to link
     */
    public void setPseudocodePanel(PseudocodePanel pseudocodePanel) {
        this.pseudocodePanel = pseudocodePanel;
    }

    /**
     * Pauses the animation process.
     */
    public void pause() {
        isPaused = true;
        updateStatus("Animation paused. Use Play to continue or Step for one step at a time.");
    }
    
    /**
     * Resumes the animation process.
     */
    public void play() {
        isPaused = false;
        isStepMode = false;
        
        // Notify any threads waiting on the step lock
        synchronized (stepLock) {
            stepLock.notifyAll();
        }
        
        updateStatus("Animation playing...");
    }
    
    /**
     * Executes a single step of the animation when in step mode.
     */
    public void step() {
        if (!isPaused) {
            pause();
            isStepMode = true;
        }
        
        // Notify the algorithm thread to proceed with one step
        synchronized (stepLock) {
            waitingForStep = false;
            stepLock.notifyAll();
        }
    }
    
    /**
     * Resets the animation to the initial state.
     */
    public void reset() {
        // Clear all colored blocks
        for (int i = 0; i < blockPanes.size(); i++) {
            resetBlockColor(i);
        }
        
        // Reset control flags
        isPaused = false;
        isStepMode = false;
        
        // Release any waiting threads
        synchronized (stepLock) {
            waitingForStep = false;
            stepLock.notifyAll();
        }
        
        // If we have the current array, redisplay it
        if (currentArray != null) {
            displayArray(currentArray);
            updateStatus("Animation reset. Ready to start.");
        }
    }
    
    /**
     * Sets the current array and algorithm name for use with reset.
     */
    public void setCurrentState(int[] array, String algorithm) {
        this.currentArray = array.clone();
        this.currentAlgorithm = algorithm;
    }
    
    /**
     * Waits for user input when in step mode or paused.
     * Should be called by algorithm implementations at key animation points.
     */
    public void waitForStep() {
        if ((isPaused && isStepMode) || isPaused) {
            synchronized (stepLock) {
                try {
                    waitingForStep = true;
                    while (isPaused && (isStepMode ? waitingForStep : true)) {
                        stepLock.wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    
    /**
     * Utility method for algorithm implementations to check animation control state
     * and pause if needed before proceeding with visualization.
     */
    public void checkPauseAndStep() {
        if (isPaused) {
            waitForStep();
        }
    }

    /**
     * Sets the animation speed multiplier.
     * @param speed The speed multiplier (0.1 to 2.0)
     */
    public void setAnimationSpeed(double speed) {
        if (speed >= 0.1 && speed <= 2.0) {
            Platform.runLater(() -> {
                animationSpeedMultiplier.set(speed);
                updateStatus("Animation speed set to " + String.format("%.1f", speed) + "x");
            });
        }
    }
}