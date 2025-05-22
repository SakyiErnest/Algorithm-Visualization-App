import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class AdaptiveVisualizerPanel extends VisualizerPanel {

    private static final int ADAPTIVE_THRESHOLD = 100;

    private enum VisualizationMode {
        STANDARD, COMPACT, BARS, MINIMAP
    }

    private BorderPane adaptiveContainer;
    private FlowPane adaptiveFlowPane;
    private Pane scrollContentPane;
    private ScrollPane viewportScrollPane;
    private HBox navigationControls;
    private ToggleButton followToggle;
    private Label viewportInfoLabel;
    private Button firstButton, prevButton, nextButton, lastButton;

    private DoubleProperty zoomFactor = new SimpleDoubleProperty(1.0);
    private int viewportStartIndex = 0;
    private int viewportEndIndex = 0;
    private int viewportSize = 100;
    private int[] lastAccessedIndices = new int[3];
    private boolean autoFollow = true;

    private int[] currentArray;
    private VisualizationMode currentMode;
    private final Map<Integer, Color> blockColorMap = new HashMap<>();

    private static final int COMPACT_BLOCK_WIDTH = 20;
    private static final int COMPACT_BLOCK_HEIGHT = 20;
    private static final int COMPACT_SPACING = 3;
    private static final int COMPACT_BLOCKS_PER_ROW = 20;

    private static final int BAR_WIDTH = 8;
    private static final int BAR_SPACING = 1;
    private static final int BAR_MIN_HEIGHT = 5;
    private static final int BAR_MAX_HEIGHT = 80;

    private static final int MINIMAP_WIDTH = 2;
    private static final int MINIMAP_SPACING = 0;
    private static final int MINIMAP_HEIGHT_MULTIPLIER = 1;

    public AdaptiveVisualizerPanel(Slider speedSlider) {
        super(speedSlider);
        initializeAdaptiveComponents();
    }

    private void initializeAdaptiveComponents() {
        adaptiveFlowPane = new FlowPane();
        adaptiveFlowPane.setHgap(COMPACT_SPACING);
        adaptiveFlowPane.setVgap(COMPACT_SPACING);
        adaptiveFlowPane.setAlignment(Pos.CENTER);
        adaptiveFlowPane.setPrefWrapLength((COMPACT_BLOCK_WIDTH + COMPACT_SPACING) * COMPACT_BLOCKS_PER_ROW);

        scrollContentPane = new Pane();
        scrollContentPane.setMinHeight(200);

        viewportScrollPane = new ScrollPane();
        viewportScrollPane.setPannable(true);
        viewportScrollPane.setFitToHeight(true);
        viewportScrollPane.setStyle("-fx-background-color: transparent;");

        Slider zoomSlider = new Slider(0.5, 5.0, 1.0);
        zoomSlider.setPrefWidth(150);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setShowTickLabels(true);
        zoomFactor.bind(zoomSlider.valueProperty());

        followToggle = new ToggleButton("Auto-Follow");
        followToggle.setSelected(true);
        followToggle.selectedProperty().addListener((obs, old, newVal) -> autoFollow = newVal);

        firstButton = new Button("⏮ First");
        firstButton.setOnAction(e -> navigateToIndex(0));

        prevButton = new Button("◀ Prev");
        prevButton.setOnAction(e -> navigateToPrevious());

        nextButton = new Button("Next ▶");
        nextButton.setOnAction(e -> navigateToNext());

        lastButton = new Button("Last ⏭");
        lastButton.setOnAction(e -> navigateToLast());

        viewportInfoLabel = new Label("");
        viewportInfoLabel.setStyle("-fx-font-style: italic;");

        navigationControls = new HBox(10);
        navigationControls.setAlignment(Pos.CENTER);
        navigationControls.getChildren().addAll(
                new Label("Zoom:"), zoomSlider,
                new Separator(Orientation.VERTICAL),
                firstButton, prevButton, nextButton, lastButton,
                new Separator(Orientation.VERTICAL),
                followToggle,
                viewportInfoLabel
        );
        navigationControls.setPadding(new Insets(10, 5, 5, 5));

        adaptiveContainer = new BorderPane();
        adaptiveContainer.setCenter(viewportScrollPane);
        adaptiveContainer.setBottom(navigationControls);

        FlowPane originalArrayContainer = getArrayContainer();
        if (originalArrayContainer != null) {
            adaptiveContainer.setBorder(originalArrayContainer.getBorder());
            adaptiveContainer.setMinHeight(originalArrayContainer.getMinHeight());
            adaptiveContainer.setPadding(originalArrayContainer.getPadding());
        }

        adaptiveContainer.setVisible(false);
        adaptiveContainer.setManaged(false);

        VBox mainContainer = getPane();
        int insertIndex = findArrayContainerIndex(mainContainer);
        if (insertIndex >= 0) {
            mainContainer.getChildren().add(insertIndex + 1, adaptiveContainer);
        }

        zoomFactor.addListener((obs, oldVal, newVal) -> {
            if (currentArray != null && currentArray.length > ADAPTIVE_THRESHOLD) {
                updateVisualization(false);
            }
        });
    }
    private int findArrayContainerIndex(VBox mainContainer) {
        for (int i = 0; i < mainContainer.getChildren().size(); i++) {
            if (mainContainer.getChildren().get(i) instanceof FlowPane) {
                return i;
            }
        }
        return -1;
    }
    private FlowPane getArrayContainer() {
        VBox mainContainer = getPane();
        for (Node node : mainContainer.getChildren()) {
            if (node instanceof FlowPane) {
                return (FlowPane) node;
            }
        }
        return null;
    }

    @Override
    public void displayArray(int[] arr) {
        currentArray = arr.clone(); // Defensive copy
        blockColorMap.clear(); // Clear previous highlights

        FlowPane originalArrayContainer = getArrayContainer();

        // Choose visualization mode based on array size
        selectVisualizationMode(arr.length);

        if (currentMode == VisualizationMode.STANDARD) {
            // Use original visualization and hide adaptive elements
            super.displayArray(arr);
            adaptiveContainer.setVisible(false);
            adaptiveContainer.setManaged(false);
            if (originalArrayContainer != null) {
                originalArrayContainer.setVisible(true);
                originalArrayContainer.setManaged(true);
            }
        } else {
            // Hide original container, show adaptive, and setup viewport
            if (originalArrayContainer != null) {
                originalArrayContainer.setVisible(false);
                originalArrayContainer.setManaged(false);
            }
            adaptiveContainer.setVisible(true);
            adaptiveContainer.setManaged(true);

            calculateViewportCapacity();
            viewportStartIndex = 0;
            resetAutoFollowTracking();
            updateVisualization(true);
        }
    }
    private void selectVisualizationMode(int arraySize) {
        if (arraySize <= ADAPTIVE_THRESHOLD) {
            currentMode = VisualizationMode.STANDARD;
        } else if (arraySize <= 300) {
            currentMode = VisualizationMode.COMPACT;
        } else if (arraySize <= 1000) {
            currentMode = VisualizationMode.BARS;
        } else {
            currentMode = VisualizationMode.MINIMAP;
        }

        // Configure scroll pane content based on mode
        viewportScrollPane.setContent(currentMode == VisualizationMode.COMPACT ? adaptiveFlowPane : scrollContentPane);
        viewportScrollPane.setFitToWidth(currentMode == VisualizationMode.COMPACT);
    }

    private void calculateViewportCapacity() {
        double availableWidth = viewportScrollPane.getWidth() > 0 ? viewportScrollPane.getWidth() : 800; // Fallback

        switch (currentMode) {
            case COMPACT:
                viewportSize = COMPACT_BLOCKS_PER_ROW * 5; // 5 rows
                break;
            case BARS:
                viewportSize = (int) (availableWidth / ((BAR_WIDTH + BAR_SPACING) * zoomFactor.get()));
                break;
            case MINIMAP:
                viewportSize = (int) (availableWidth / ((MINIMAP_WIDTH + MINIMAP_SPACING) * zoomFactor.get()));
                break;
            default:
                viewportSize = MAX_BLOCKS_PER_ROW * 5; // Standard mode
        }
        viewportSize = Math.max(20, viewportSize); // Minimum size
    }

    private void updateVisualization(boolean animate) {
        if (currentArray == null || currentArray.length == 0) return;

        viewportEndIndex = Math.min(viewportStartIndex + viewportSize, currentArray.length);

        Platform.runLater(() -> {
            switch (currentMode) {
                case COMPACT:   updateCompactVisualization(animate); break;
                case BARS:      updateBarVisualization(animate);     break;
                case MINIMAP:   updateMinimapVisualization(animate); break;
                default: /* Should not happen */                   break;
            }
            updateViewportInfo();
        });
    }

    private void updateCompactVisualization(boolean animate) {
        adaptiveFlowPane.getChildren().clear();
        for (int i = viewportStartIndex; i < viewportEndIndex; i++) {
            Color blockColor = blockColorMap.getOrDefault(i, DEFAULT_BLOCK_COLOR);
            StackPane block = createCompactBlock(i, currentArray[i], blockColor);
            adaptiveFlowPane.getChildren().add(block);
            if (animate) animateElementEntrance(block, i - viewportStartIndex);
        }
    }

    private void updateBarVisualization(boolean animate) {
        scrollContentPane.getChildren().clear();
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        for (int val : currentArray) {
            minValue = Math.min(minValue, val);
            maxValue = Math.max(maxValue, val);
        }
        if(minValue >= maxValue) maxValue = minValue + 1; // Prevent division by zero

        double xPos = 10;
        for (int i = viewportStartIndex; i < viewportEndIndex; i++) {
            Color blockColor = blockColorMap.getOrDefault(i, DEFAULT_BLOCK_COLOR);
            double ratio = (double) (currentArray[i] - minValue) / (maxValue - minValue);
            int barHeight = (int) (BAR_MIN_HEIGHT + ratio * (BAR_MAX_HEIGHT - BAR_MIN_HEIGHT));
            StackPane bar = createBarElement(i, currentArray[i], barHeight, blockColor);
            bar.setLayoutX(xPos);
            bar.setLayoutY(0);
            scrollContentPane.getChildren().add(bar);
            if (animate) animateElementEntrance(bar, i - viewportStartIndex);
            xPos += (BAR_WIDTH + BAR_SPACING) * zoomFactor.get();
        }
        scrollContentPane.setPrefWidth(xPos + 10);
        scrollContentPane.setPrefHeight(BAR_MAX_HEIGHT + 30);
    }
    private void updateMinimapVisualization(boolean animate) {
        scrollContentPane.getChildren().clear();

        // Find min/max for scaling
        int minValue = Integer.MAX_VALUE;
        int maxValue = Integer.MIN_VALUE;
        for (int value : currentArray) {
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
        }
        if (minValue >= maxValue) maxValue = minValue + 1;

        double xPos = 10; // Initial position with padding

        // Create minimap elements for current viewport
        for (int i = viewportStartIndex; i < viewportEndIndex; i++) {
            // Get color for this element
            Color blockColor = blockColorMap.getOrDefault(i, DEFAULT_BLOCK_COLOR);

            // Calculate height based on value (1-6 pixels)
            double ratio = (double)(currentArray[i] - minValue) / (maxValue - minValue);
            int height = (int)(1 + ratio * 5);

            // Create and position minimap element
            Rectangle minimap = createMinimapElement(i, currentArray[i], height, blockColor);
            minimap.setLayoutX(xPos);
            minimap.setLayoutY(0);
            scrollContentPane.getChildren().add(minimap);

            if (animate) {
                animateElementEntrance(minimap, i - viewportStartIndex);
            }

            // Update position for next element
            xPos += (MINIMAP_WIDTH + MINIMAP_SPACING) * zoomFactor.get();
        }

        // Set content width
        scrollContentPane.setPrefWidth(xPos + 10);
        scrollContentPane.setPrefHeight(30);
    }
    private StackPane createCompactBlock(int index, int value, Color color) {
        Rectangle rect = new Rectangle(COMPACT_BLOCK_WIDTH * zoomFactor.get(), COMPACT_BLOCK_HEIGHT * zoomFactor.get());
        rect.setArcWidth(3 * zoomFactor.get());
        rect.setArcHeight(3 * zoomFactor.get());
        rect.setFill(color);
        rect.setStroke(color.darker());

        Label label = new Label(String.valueOf(value));
        label.setFont(Font.font("System", FontWeight.BOLD, 9 * zoomFactor.get()));
        label.setTextFill(Color.WHITE);

        StackPane block = new StackPane(rect, label);
        Tooltip.install(rect, new Tooltip("Index: " + index + ", Value: " + value));
        return block;
    }

    private StackPane createBarElement(int index, int value, int height, Color color) {
        Rectangle rect = new Rectangle(BAR_WIDTH * zoomFactor.get(), height * zoomFactor.get());
        rect.setArcWidth(2 * zoomFactor.get());
        rect.setArcHeight(2 * zoomFactor.get());
        rect.setFill(color);
        rect.setStroke(color.darker());

        StackPane block = new StackPane(rect);
        block.setAlignment(Pos.BOTTOM_CENTER);
        block.setPrefHeight((BAR_MAX_HEIGHT + 10) * zoomFactor.get());
        Tooltip.install(rect, new Tooltip("Index: " + index + ", Value: " + value));
        return block;
    }

    private Rectangle createMinimapElement(int index, int value, int height, Color color) {
        Rectangle rect = new Rectangle(MINIMAP_WIDTH * zoomFactor.get(), height * MINIMAP_HEIGHT_MULTIPLIER * zoomFactor.get());
        rect.setFill(color);
        Tooltip.install(rect, new Tooltip("Index: " + index + ", Value: " + value));
        return rect;
    }

    private void animateElementEntrance(Node element, int delayIndex) {
        element.setOpacity(0);
        element.setTranslateY(5);
        FadeTransition fade = new FadeTransition(Duration.millis(100), element);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition translate = new TranslateTransition(Duration.millis(100), element);
        translate.setFromY(5);
        translate.setToY(0);
        ParallelTransition animation = new ParallelTransition(fade, translate);
        animation.setDelay(Duration.millis(5 * delayIndex));
        animation.play();
    }

    private void updateViewportInfo() {
        String infoText = currentArray.length <= ADAPTIVE_THRESHOLD ?
                "Viewing: All elements" :
                String.format("Viewing: %d-%d of %d (%.1f%%)", viewportStartIndex, viewportEndIndex - 1, currentArray.length,
                        (double) (viewportEndIndex - viewportStartIndex) / currentArray.length * 100);
        viewportInfoLabel.setText(infoText);
    }

    @Override
    public void highlightBlock(int index, Color color) {
        if (currentArray == null || index < 0 || index >= currentArray.length) return;

        if (currentArray.length <= ADAPTIVE_THRESHOLD) {
            super.highlightBlock(index, color); // Use original for small arrays
        } else {
            blockColorMap.put(index, color); // Store highlight
            if (autoFollow && (index < viewportStartIndex || index >= viewportEndIndex)) {
                trackAccessedIndex(index);
                focusOnRecentActivity();
            } else {
                updateVisualization(false);
            }
        }
    }

    private void trackAccessedIndex(int index) {
        for (int i = lastAccessedIndices.length - 1; i > 0; i--) {
            lastAccessedIndices[i] = lastAccessedIndices[i - 1];
        }
        lastAccessedIndices[0] = index;
    }
    private void resetAutoFollowTracking() {
        for (int i = 0; i < lastAccessedIndices.length; i++) {
            lastAccessedIndices[i] = -1; // Reset
        }
    }

    private void focusOnRecentActivity() {
        if (currentArray == null || currentArray.length <= viewportSize) return;

        int focusIndex = -1;
        for (int idx : lastAccessedIndices) {
            if (idx >= 0 && idx < currentArray.length) {
                focusIndex = idx;
                break;
            }
        }
        if (focusIndex != -1) navigateToIndex(focusIndex);
    }
    private void navigateToIndex(int index) {
        if (currentArray == null) return;
        index = Math.max(0, Math.min(index, currentArray.length - 1));

        if (currentMode == VisualizationMode.COMPACT) {
            viewportStartIndex = (index / viewportSize) * viewportSize; // Page-based navigation
        } else {
            viewportStartIndex = Math.max(0, index - viewportSize / 2); // Centered viewport
        }
        viewportStartIndex = Math.max(0, Math.min(viewportStartIndex, currentArray.length - viewportSize));
        viewportEndIndex = Math.min(viewportStartIndex + viewportSize, currentArray.length);
        updateVisualization(false);
    }

    private void navigateToPrevious() {
        autoFollow = false;
        followToggle.setSelected(false);
        viewportStartIndex = Math.max(0, viewportStartIndex - viewportSize);
        viewportEndIndex = Math.min(viewportStartIndex + viewportSize, currentArray.length);
        updateVisualization(false);
    }

    private void navigateToNext() {
        if (currentArray == null) return;
        autoFollow = false;
        followToggle.setSelected(false);
        viewportStartIndex = Math.min(currentArray.length - 1, viewportStartIndex + viewportSize);
        viewportEndIndex = Math.min(viewportStartIndex + viewportSize, currentArray.length);

        if (viewportStartIndex >= currentArray.length - viewportSize) {
            viewportStartIndex = Math.max(0, currentArray.length - viewportSize);
            viewportEndIndex = currentArray.length;
        }
        updateVisualization(false);
    }

    private void navigateToLast() {
        if (currentArray == null) return;
        autoFollow = false;
        followToggle.setSelected(false);
        viewportStartIndex = Math.max(0, currentArray.length - viewportSize);
        viewportEndIndex = currentArray.length;
        updateVisualization(false);
    }

    @Override
    public void resetBlockColor(int index) {
        if (currentArray == null || index < 0 || index >= currentArray.length) return;

        if (currentArray.length <= ADAPTIVE_THRESHOLD) {
            super.resetBlockColor(index);
        } else {
            blockColorMap.remove(index); // Remove highlight
            updateVisualization(false);
        }
    }
    public void resetAllBlockColors() {
        if (currentArray != null && currentArray.length > ADAPTIVE_THRESHOLD) {
            blockColorMap.clear(); //Clear all
            updateVisualization(false);
        }
    }
}