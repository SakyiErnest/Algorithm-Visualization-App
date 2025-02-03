import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

public class PseudocodePanel {
    private final VBox pane;
    private final VBox codeContainer;
    private final List<Label> lines;
    private final ScrollPane scrollPane;

    // Constants for styling
    private static final double LINE_SPACING = 8;
    private static final double PADDING = 15;
    private static final String FONT_FAMILY = "JetBrains Mono";
    private static final double FONT_SIZE = 14;
    private static final Duration HIGHLIGHT_TRANSITION_DURATION = Duration.millis(300);

    public PseudocodePanel() {
        this.lines = new ArrayList<>();

        // Initialize main container
        this.pane = new VBox();
        this.pane.getStyleClass().add("pseudocode-panel");

        // Initialize code container
        this.codeContainer = new VBox(LINE_SPACING);
        this.codeContainer.getStyleClass().add("code-container");
        this.codeContainer.setPadding(new Insets(PADDING));

        // Initialize scroll pane
        this.scrollPane = new ScrollPane(codeContainer);
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setFitToHeight(true);

        // Add title and scroll pane to main container
        Label titleLabel = createTitleLabel("Algorithm Pseudocode");
        this.pane.getChildren().addAll(titleLabel, scrollPane);

        setupStyles();
    }

    private void setupStyles() {
        // Apply CSS styles
        pane.setStyle("""
            -fx-background-color: #2b2b2b;
            -fx-border-radius: 10;
            -fx-background-radius: 10;
            """);

        scrollPane.setStyle("""
            -fx-background: #2b2b2b;
            -fx-background-color: transparent;
            -fx-padding: 0;
            -fx-border-width: 0;
            """);

        codeContainer.setStyle("""
            -fx-background-color: #1e1e1e;
            -fx-border-radius: 8;
            -fx-background-radius: 8;
            """);
    }

    private Label createTitleLabel(String title) {
        Label label = new Label(title);
        label.setFont(Font.font(FONT_FAMILY, FONT_SIZE + 2));
        label.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-weight: bold;
            -fx-padding: 10 15 10 15;
            """);
        return label;
    }

    public void setPseudocode(String[] codeLines) {
        Platform.runLater(() -> {
            codeContainer.getChildren().clear();
            lines.clear();

            for (int i = 0; i < codeLines.length; i++) {
                HBox lineContainer = createLineContainer(i + 1, codeLines[i]);
                lines.add((Label) lineContainer.getChildren().get(1));
                codeContainer.getChildren().add(lineContainer);
            }
        });
    }

    private HBox createLineContainer(int lineNumber, String code) {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(2, 10, 2, 10));
        container.getStyleClass().add("line-container");

        // Line number label
        Label numberLabel = new Label(String.format("%2d", lineNumber));
        numberLabel.setFont(Font.font(FONT_FAMILY, FONT_SIZE));
        numberLabel.setStyle("""
            -fx-text-fill: #6c6c6c;
            -fx-min-width: 25;
            """);

        // Code label
        Label codeLabel = new Label(code);
        codeLabel.setFont(Font.font(FONT_FAMILY, FONT_SIZE));
        codeLabel.setStyle("-fx-text-fill: #d4d4d4;");

        container.getChildren().addAll(numberLabel, codeLabel);

        // Corrected hover effect
        container.setOnMouseEntered(e -> container.setStyle(
                "-fx-background-color: #3c3c3c;"
        ));
        container.setOnMouseExited(e -> container.setStyle(
                "-fx-background-color: transparent;"
        ));

        return container;
    }

    public void highlightLine(int index) {
        Platform.runLater(() -> {
            clearHighlights();
            if (index >= 0 && index < lines.size()) {
                Label line = lines.get(index);
                HBox container = (HBox) line.getParent();

                // Create highlight animation
                FadeTransition highlight = new FadeTransition(
                        HIGHLIGHT_TRANSITION_DURATION, container);
                container.setStyle("""
                    -fx-background-color: #2d4c60;
                    -fx-background-radius: 4;
                    """);

                // Ensure highlighted line is visible
                scrollPane.setVvalue(
                        (double) index / (lines.size() - 1)
                );
            }
        });
    }

    public void clearHighlights() {
        Platform.runLater(() -> {
            for (Label line : lines) {
                HBox container = (HBox) line.getParent();
                container.setStyle("-fx-background-color: transparent;");
            }
        });
    }

    public VBox getPane() {
        return pane;
    }
}
