import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class VisualizerPanel {

    private VBox mainContainer;
    private FlowPane arrayContainer;
    private Label statusLabel;
    private List<StackPane> blockPanes;

    public static final int BLOCK_WIDTH = 30;
    public static final int BLOCK_HEIGHT = 30;
    public static final int BLOCK_SPACING = 5;
    // Base animation duration in milliseconds. Can be scaled with the speed slider.
    private static final double ANIMATION_DURATION = 500;
    private static final Color DEFAULT_BLOCK_COLOR = Color.LIGHTBLUE;
    private static final Color COMPARING_COLOR = Color.YELLOW;
    private static final Color SORTED_COLOR = Color.LIGHTGREEN;
    private static final Color SELECTED_COLOR = Color.ORANGE;
    private static final int MAX_BLOCKS_PER_ROW = 15;

    private boolean isAnimating = false;

    public VisualizerPanel() {
        mainContainer = new VBox(10);
        mainContainer.setPadding(new Insets(10));
        mainContainer.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 5; -fx-padding: 15;"
                + " -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5,0,0,2);");

        // Title & Description
        VBox headerBox = new VBox(5);
        Label header = new Label("Algorithm Visualization");
        header.setFont(Font.font("System", FontWeight.BOLD, 16));
        Label description = new Label("Watch the sorting process step by step");
        description.setFont(Font.font("System", FontPosture.ITALIC, 12));
        headerBox.getChildren().addAll(header, description);

        // FlowPane for Blocks
        arrayContainer = new FlowPane();
        arrayContainer.setHgap(BLOCK_SPACING);
        arrayContainer.setVgap(BLOCK_SPACING);
        arrayContainer.setPrefWrapLength((BLOCK_WIDTH + BLOCK_SPACING) * MAX_BLOCKS_PER_ROW);
        arrayContainer.setAlignment(Pos.CENTER);

        // Status Label
        statusLabel = new Label("Algorithm not started.");
        statusLabel.setFont(Font.font("System", 14));

        // Animation Speed Control
        Slider speedSlider = new Slider(0.1, 2.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        Label speedLabel = new Label("Animation Speed:");

        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.getChildren().addAll(speedLabel, speedSlider);

        // Legend
        HBox legend = createLegend();

        // Assembling UI
        mainContainer.getChildren().addAll(headerBox, controlsBox, arrayContainer, legend, statusLabel);
        blockPanes = new ArrayList<>();
    }

    private HBox createLegend() {
        HBox legend = new HBox(10);
        legend.setAlignment(Pos.CENTER);
        legend.setPadding(new Insets(5));

        legend.getChildren().addAll(
                createLegendItem("Unsorted", DEFAULT_BLOCK_COLOR),
                createLegendItem("Comparing", COMPARING_COLOR),
                createLegendItem("Selected", SELECTED_COLOR),
                createLegendItem("Sorted", SORTED_COLOR)
        );
        return legend;
    }

    private HBox createLegendItem(String text, Color color) {
        Rectangle rect = new Rectangle(15, 15);
        rect.setFill(color);
        rect.setStroke(Color.BLACK);
        Label label = new Label(text);
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER);
        item.getChildren().addAll(rect, label);
        return item;
    }

    public VBox getPane() {
        return mainContainer;
    }

    public void displayArray(int[] arr) {
        Platform.runLater(() -> {
            arrayContainer.getChildren().clear();
            blockPanes.clear();
            for (int value : arr) {
                StackPane block = createBlock(value);
                blockPanes.add(block);
            }
            arrayContainer.getChildren().addAll(blockPanes);
        });
    }

    private StackPane createBlock(int value) {
        Rectangle rect = new Rectangle(BLOCK_WIDTH, BLOCK_HEIGHT);
        rect.setArcWidth(5);
        rect.setArcHeight(5);
        rect.setFill(DEFAULT_BLOCK_COLOR);
        rect.setStroke(Color.DARKBLUE);

        rect.setOnMouseEntered(e -> {
            rect.setEffect(new DropShadow(10, Color.GRAY));
            rect.setScaleX(1.1);
            rect.setScaleY(1.1);
        });

        rect.setOnMouseExited(e -> {
            rect.setEffect(null);
            rect.setScaleX(1.0);
            rect.setScaleY(1.0);
        });

        Label label = new Label(String.valueOf(value));
        label.setFont(Font.font("System", 12));

        StackPane block = new StackPane(rect, label);
        Tooltip.install(block, new Tooltip("Value: " + value));

        return block;
    }

    public void updateStatus(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }

    /**
     * Enhanced swap animation with combined translate, scale pulse, and fade transitions.
     */
    public void animateSwap(int i, int j) {
        if (isAnimating || i < 0 || j < 0 || i >= blockPanes.size() || j >= blockPanes.size() || i == j) return;
        isAnimating = true;

        StackPane blockI = blockPanes.get(i);
        StackPane blockJ = blockPanes.get(j);

        double distance = (BLOCK_WIDTH + BLOCK_SPACING) * (j - i);

        // Translate transitions for swapping
        TranslateTransition translateI = new TranslateTransition(Duration.millis(ANIMATION_DURATION), blockI);
        translateI.setByX(distance);
        TranslateTransition translateJ = new TranslateTransition(Duration.millis(ANIMATION_DURATION), blockJ);
        translateJ.setByX(-distance);

        // Scale pulse effect to emphasize action
        ScaleTransition scaleUpI = new ScaleTransition(Duration.millis(ANIMATION_DURATION / 2), blockI);
        scaleUpI.setToX(1.2);
        scaleUpI.setToY(1.2);
        ScaleTransition scaleDownI = new ScaleTransition(Duration.millis(ANIMATION_DURATION / 2), blockI);
        scaleDownI.setToX(1.0);
        scaleDownI.setToY(1.0);

        ScaleTransition scaleUpJ = new ScaleTransition(Duration.millis(ANIMATION_DURATION / 2), blockJ);
        scaleUpJ.setToX(1.2);
        scaleUpJ.setToY(1.2);
        ScaleTransition scaleDownJ = new ScaleTransition(Duration.millis(ANIMATION_DURATION / 2), blockJ);
        scaleDownJ.setToX(1.0);
        scaleDownJ.setToY(1.0);

        // Fade transition for extra visual feedback
        FadeTransition fadeOutI = new FadeTransition(Duration.millis(ANIMATION_DURATION / 2), blockI);
        fadeOutI.setFromValue(1.0);
        fadeOutI.setToValue(0.7);
        FadeTransition fadeInI = new FadeTransition(Duration.millis(ANIMATION_DURATION / 2), blockI);
        fadeInI.setFromValue(0.7);
        fadeInI.setToValue(1.0);

        FadeTransition fadeOutJ = new FadeTransition(Duration.millis(ANIMATION_DURATION / 2), blockJ);
        fadeOutJ.setFromValue(1.0);
        fadeOutJ.setToValue(0.7);
        FadeTransition fadeInJ = new FadeTransition(Duration.millis(ANIMATION_DURATION / 2), blockJ);
        fadeInJ.setFromValue(0.7);
        fadeInJ.setToValue(1.0);

        // Sequence transitions: pulse (scale & fade out/in) during translation
        SequentialTransition seqI = new SequentialTransition(
                new ParallelTransition(scaleUpI, fadeOutI),
                new ParallelTransition(scaleDownI, fadeInI)
        );
        SequentialTransition seqJ = new SequentialTransition(
                new ParallelTransition(scaleUpJ, fadeOutJ),
                new ParallelTransition(scaleDownJ, fadeInJ)
        );

        // Execute the swap animation in parallel with translation and scaling/fading effects.
        ParallelTransition parallelTransition = new ParallelTransition(translateI, translateJ, seqI, seqJ);
        parallelTransition.setOnFinished(e -> {
            Platform.runLater(() -> {
                // Reset translations after animation
                blockI.setTranslateX(0);
                blockJ.setTranslateX(0);
                // Swap the blocks in the list
                StackPane temp = blockPanes.get(i);
                blockPanes.set(i, blockPanes.get(j));
                blockPanes.set(j, temp);
                arrayContainer.getChildren().clear();
                arrayContainer.getChildren().addAll(blockPanes);
                isAnimating = false;
            });
        });
        parallelTransition.play();
    }

    /**
     * Enhanced shifting animation using a translate transition paired with a subtle fade effect.
     */
    public void animateShiftRight(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= blockPanes.size() || toIndex >= blockPanes.size()) {
            return;
        }

        StackPane block = blockPanes.get(fromIndex);
        double shiftDistance = (BLOCK_WIDTH + BLOCK_SPACING) * (toIndex - fromIndex);

        TranslateTransition translate = new TranslateTransition(Duration.millis(ANIMATION_DURATION * 0.6), block);
        translate.setByX(shiftDistance);

        FadeTransition fade = new FadeTransition(Duration.millis(ANIMATION_DURATION * 0.6), block);
        fade.setFromValue(1.0);
        fade.setToValue(0.8);

        FadeTransition fadeBack = new FadeTransition(Duration.millis(ANIMATION_DURATION * 0.6), block);
        fadeBack.setFromValue(0.8);
        fadeBack.setToValue(1.0);

        ParallelTransition shiftAnimation = new ParallelTransition(translate, fade);
        SequentialTransition seq = new SequentialTransition(shiftAnimation, fadeBack);
        seq.setOnFinished(e -> {
            block.setTranslateX(0);
            Platform.runLater(() -> {
                StackPane removedBlock = blockPanes.remove(fromIndex);
                blockPanes.add(toIndex, removedBlock);
                arrayContainer.getChildren().clear();
                arrayContainer.getChildren().addAll(blockPanes);
            });
        });
        seq.play();
    }

    /**
     * Updates the color of a specific block.
     */
    public void highlightBlock(int index, Color color) {
        if (index < 0 || index >= blockPanes.size()) return;
        Platform.runLater(() -> {
            StackPane block = blockPanes.get(index);
            ((Rectangle) block.getChildren().get(0)).setFill(color);
        });
    }

    public void clear() {
        Platform.runLater(() -> {
            arrayContainer.getChildren().clear();
            blockPanes.clear();
            statusLabel.setText("Step-by-Step Visualization");
        });
    }
}