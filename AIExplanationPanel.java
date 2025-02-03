/* File: AIExplanationPanel.java */
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class AIExplanationPanel {
    private VBox pane;
    private Label explanationLabel;

    public AIExplanationPanel() {
        pane = new VBox(10);
        pane.setPadding(new Insets(10));
        pane.getStyleClass().add("ai-explanation-panel");

        explanationLabel = new Label("AI Hints will appear here...");
        explanationLabel.setFont(Font.font("Monospaced", 14));
        explanationLabel.setWrapText(true);
        pane.getChildren().add(explanationLabel);
    }

    public VBox getPane() {
        return pane;
    }

    public void updateExplanation(String text) {
        Platform.runLater(() -> explanationLabel.setText(text));
    }
}
