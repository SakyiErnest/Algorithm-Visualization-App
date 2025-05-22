import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A panel that displays algorithm pseudocode with syntax highlighting,
 * line-by-line execution highlighting, and explanatory tooltips.
 */
public class PseudocodePanel {
    // UI components
    private final VBox pane;
    private final VBox codeContainer;
    private final List<HBox> lineContainers;
    private final ScrollPane scrollPane;

    // State tracking - make these class fields so they can be modified in lambdas
    private int highlightedLineIndex = -1;
    private int previousHighlightedLine = -1;
    private Timeline blinkAnimation;

    // Style constants
    private static final double LINE_SPACING = 10;
    private static final double PADDING = 20;
    private static final String MONO_FONT = "JetBrains Mono, Consolas, monospace";
    private static final double FONT_SIZE = 14;
    private static final Duration HIGHLIGHT_TRANSITION_DURATION = Duration.millis(400);

    // CSS classes
    private static final String PANEL_CLASS = "pseudocode-panel";
    private static final String CODE_CONTAINER_CLASS = "code-container";
    private static final String LINE_CONTAINER_CLASS = "line-container";
    private static final String LINE_NUMBER_CLASS = "line-number";
    private static final String CODE_LINE_CLASS = "code-line";
    private static final String TITLE_CLASS = "title-label";
    private static final String HIGHLIGHT_CLASS = "highlighted-line";
    private static final String EXECUTED_LINE_CLASS = "executed-line";
    private static final String KEYWORD_CLASS = "keyword";
    private static final String COMMENT_CLASS = "comment";
    private static final String OPERATOR_CLASS = "operator";

    // Syntax highlighting patterns
    private static final Pattern KEYWORD_PATTERN = Pattern.compile(
            "\\b(if|else|for|while|do|return|break|continue|switch|case|default|function|procedure|" +
                    "then|end|loop|until|repeat|array|create|swap|partition|sort|search|merge|split|pivot)\\b",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern COMMENT_PATTERN = Pattern.compile("//.*|/\\*.*\\*/");
    private static final Pattern OPERATOR_PATTERN = Pattern.compile("[=<>!+\\-*/()\\[\\]{}:;,%]");

    /**
     * Creates a new pseudocode panel with all necessary components.
     */
    public PseudocodePanel() {
        // Initialize state
        this.lineContainers = new ArrayList<>();

        // Create main container
        this.pane = new VBox(10);
        this.pane.getStyleClass().add(PANEL_CLASS);
        this.pane.setPadding(new Insets(10, 0, 10, 0));

        // Create code container
        this.codeContainer = new VBox(LINE_SPACING);
        this.codeContainer.getStyleClass().add(CODE_CONTAINER_CLASS);
        this.codeContainer.setPadding(new Insets(PADDING));

        // Create scroll pane
        this.scrollPane = new ScrollPane(codeContainer);
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setFitToHeight(true);
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        this.scrollPane.getStyleClass().add("pseudocode-scrollpane");

        // Create header
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(5, 15, 5, 15));

        Label titleLabel = createTitleLabel("Algorithm Pseudocode");
        titleBox.getChildren().add(titleLabel);

        // Assemble components
        this.pane.getChildren().addAll(titleBox, scrollPane);

        // Apply styles
        applyStyles();

        // Initialize blink animation for highlighted lines
        initializeBlinkAnimation();
    }

    /**
     * Creates a stylized title label.
     */
    private Label createTitleLabel(String title) {
        Label label = new Label(title);
        label.getStyleClass().add(TITLE_CLASS);
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        return label;
    }

    /**
     * Applies CSS styles to the panel.
     */
    private void applyStyles() {
        try {
            pane.getStylesheets().add(getClass().getResource("pseudocode-panel.css").toExternalForm());
        } catch (Exception e) {
            // If CSS file not found, apply inline styles
            applyInlineStyles();
        }
    }

    /**
     * Applies fallback inline styles if CSS file is not found.
     */
    private void applyInlineStyles() {
        pane.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px;");
        codeContainer.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 4px; " +
                "-fx-border-color: #dee2e6; -fx-border-radius: 4px;");
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
    }

    /**
     * Sets up the blinking animation for highlighted lines.
     */
    private void initializeBlinkAnimation() {
        blinkAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, e -> {
                    if (highlightedLineIndex >= 0 && highlightedLineIndex < lineContainers.size()) {
                        HBox container = lineContainers.get(highlightedLineIndex);
                        container.setStyle("-fx-background-color: rgba(25, 125, 225, 0.3);");
                    }
                }),
                new KeyFrame(Duration.millis(600), e -> {
                    if (highlightedLineIndex >= 0 && highlightedLineIndex < lineContainers.size()) {
                        HBox container = lineContainers.get(highlightedLineIndex);
                        container.setStyle("-fx-background-color: rgba(25, 125, 225, 0.15);");
                    }
                }),
                new KeyFrame(Duration.millis(1200))
        );
        blinkAnimation.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Sets the pseudocode content with line-by-line explanations.
     *
     * @param codeLines Array of pseudocode lines to display
     * @param explanations Array of explanations for each line
     */
    public void setPseudocode(String[] codeLines, String[] explanations) {
        if (explanations != null && codeLines.length != explanations.length) {
            throw new IllegalArgumentException("Code lines and explanations arrays must have the same length.");
        }

        // Make copies of the arrays to ensure they're effectively final for the lambda
        final String[] finalCodeLines = Arrays.copyOf(codeLines, codeLines.length);
        final String[] finalExplanations = explanations == null ? null :
                Arrays.copyOf(explanations, explanations.length);

        Platform.runLater(() -> {
            // Clear existing content
            codeContainer.getChildren().clear();
            lineContainers.clear();
            highlightedLineIndex = -1;
            previousHighlightedLine = -1;

            // Special handling for empty code
            if (finalCodeLines.length == 0) {
                Label emptyLabel = new Label("No pseudocode available for this algorithm");
                emptyLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #6c757d;");
                codeContainer.getChildren().add(emptyLabel);
                return;
            }

            // Add algorithm name as header if first line looks like a title
            int startIndex = 0;
            if (!finalCodeLines[0].trim().isEmpty() &&
                    !finalCodeLines[0].trim().startsWith("//") &&
                    !finalCodeLines[0].contains("(")) {

                HBox headerBox = new HBox();
                headerBox.setAlignment(Pos.CENTER);
                headerBox.setPadding(new Insets(0, 0, 10, 0));

                Label algorithmName = new Label(finalCodeLines[0].trim());
                algorithmName.setFont(Font.font("System", FontWeight.BOLD, FONT_SIZE + 1));
                algorithmName.setStyle("-fx-text-fill: #2c3e50;");

                headerBox.getChildren().add(algorithmName);
                codeContainer.getChildren().add(headerBox);

                // Skip the first line in main code display
                startIndex = 1;
            }

            // Process indentation to detect code structure
            int[] indentLevels = calculateIndentLevels(finalCodeLines);

            // Create line containers
            for (int i = startIndex; i < finalCodeLines.length; i++) {
                final int displayIndex = i - startIndex;
                final String explanation = finalExplanations != null && i < finalExplanations.length ?
                        finalExplanations[i] : "";

                HBox lineContainer = createLineContainer(
                        displayIndex + 1,                // Line number
                        finalCodeLines[i],              // Code line
                        explanation,                    // Explanation
                        indentLevels[i]                 // Indent level
                );

                lineContainers.add(lineContainer);
                codeContainer.getChildren().add(lineContainer);
            }

            // Stop any ongoing animations
            if (blinkAnimation != null) {
                blinkAnimation.stop();
            }
        });
    }

    /**
     * Calculates indentation levels for code structure visualization.
     */
    private int[] calculateIndentLevels(String[] lines) {
        int[] indentLevels = new int[lines.length];
        int currentIndent = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            // Detect decrease in indentation (end of block)
            if (line.startsWith("}") || line.startsWith("end") ||
                    line.startsWith("endif") || line.startsWith("endwhile") ||
                    line.startsWith("endfor")) {
                currentIndent = Math.max(0, currentIndent - 1);
            }

            indentLevels[i] = currentIndent;

            // Detect increase in indentation (start of block)
            if (line.endsWith("{") || line.endsWith("then") || line.endsWith(":") ||
                    line.contains("do") || (line.contains("for") && !line.contains("endfor"))) {
                currentIndent++;
            }
        }

        return indentLevels;
    }

    /**
     * Creates a line container with syntax highlighting and indentation.
     */
    private HBox createLineContainer(int lineNumber, String code, String explanation, int indentLevel) {
        // Main container
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(4, 10, 4, 10));
        container.getStyleClass().add(LINE_CONTAINER_CLASS);

        // Line number label
        Label numberLabel = new Label(String.format("%2d", lineNumber));
        numberLabel.setPrefWidth(25);
        numberLabel.getStyleClass().add(LINE_NUMBER_CLASS);
        numberLabel.setAlignment(Pos.CENTER_RIGHT);

        // Indentation visualization
        HBox indentVisual = new HBox();
        indentVisual.setSpacing(3);
        indentVisual.setPadding(new Insets(0, 8, 0, 0));

        for (int i = 0; i < indentLevel; i++) {
            Rectangle indentMark = new Rectangle(2, 16);
            indentMark.setFill(Color.web("#ced4da"));
            indentMark.setArcWidth(1);
            indentMark.setArcHeight(1);
            indentVisual.getChildren().add(indentMark);
        }

        // Code text with syntax highlighting
        TextFlow codeFlow = createSyntaxHighlightedText(code);
        codeFlow.getStyleClass().add(CODE_LINE_CLASS);
        HBox.setHgrow(codeFlow, Priority.ALWAYS);

        // Assemble line
        container.getChildren().addAll(numberLabel, indentVisual, codeFlow);

        // Add tooltip for explanation if available
        if (explanation != null && !explanation.isEmpty()) {
            Tooltip tooltip = new Tooltip(explanation);
            tooltip.setFont(Font.font(MONO_FONT, FONT_SIZE - 1));
            tooltip.setWrapText(true);
            tooltip.setPrefWidth(400);
            tooltip.setShowDelay(Duration.millis(500));
            tooltip.setHideDelay(Duration.millis(200));

            // Add visual indicator for lines with explanations
            Label infoIcon = new Label(" ℹ");
            infoIcon.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 11px;");
            container.getChildren().add(infoIcon);

            Tooltip.install(container, tooltip);
        }

        // Add hover effect
        setupHoverEffect(container);

        return container;
    }

    /**
     * Creates syntax-highlighted text elements from a code line.
     */
    private TextFlow createSyntaxHighlightedText(String code) {
        TextFlow textFlow = new TextFlow();

        // Handle comments (they take precedence over other highlighting)
        Matcher commentMatcher = COMMENT_PATTERN.matcher(code);
        if (commentMatcher.find()) {
            // If comment is found, split the line
            int commentStart = commentMatcher.start();
            if (commentStart > 0) {
                // Process code before comment
                String beforeComment = code.substring(0, commentStart);
                addHighlightedTextParts(textFlow, beforeComment);
            }

            // Add the comment
            Text commentText = new Text(code.substring(commentStart));
            commentText.getStyleClass().add(COMMENT_CLASS);
            textFlow.getChildren().add(commentText);
        } else {
            // No comment, process the whole line
            addHighlightedTextParts(textFlow, code);
        }

        return textFlow;
    }

    /**
     * Adds syntax-highlighted text parts to a TextFlow.
     */
    private void addHighlightedTextParts(TextFlow flow, String text) {
        List<TextSegment> segments = new ArrayList<>();
        segments.add(new TextSegment(text, 0, text.length(), "normal"));

        // Find keywords
        Matcher keywordMatcher = KEYWORD_PATTERN.matcher(text);
        while (keywordMatcher.find()) {
            splitSegment(segments, keywordMatcher.start(), keywordMatcher.end(), KEYWORD_CLASS);
        }

        // Find operators
        Matcher operatorMatcher = OPERATOR_PATTERN.matcher(text);
        while (operatorMatcher.find()) {
            splitSegment(segments, operatorMatcher.start(), operatorMatcher.end(), OPERATOR_CLASS);
        }

        // Sort segments by start position
        segments.sort(Comparator.comparingInt(s -> s.start));

        // Add all segments to the flow
        for (TextSegment segment : segments) {
            if (segment.end > segment.start) {
                Text textNode = new Text(text.substring(segment.start, segment.end));
                if (!segment.styleClass.equals("normal")) {
                    textNode.getStyleClass().add(segment.styleClass);
                }
                flow.getChildren().add(textNode);
            }
        }
    }

    /**
     * Helper class to represent a segment of text with styling.
     */
    private static class TextSegment {
        final int start;
        final int end;
        final String styleClass;

        TextSegment(String text, int start, int end, String styleClass) {
            this.start = start;
            this.end = end;
            this.styleClass = styleClass;
        }
    }

    /**
     * Splits a text segment for syntax highlighting.
     */
    private void splitSegment(List<TextSegment> segments, int start, int end, String styleClass) {
        List<TextSegment> newSegments = new ArrayList<>();

        for (TextSegment segment : segments) {
            // Skip if no overlap
            if (segment.end <= start || segment.start >= end) {
                newSegments.add(segment);
                continue;
            }

            // Handle segment before match
            if (segment.start < start) {
                newSegments.add(new TextSegment("", segment.start, start, segment.styleClass));
            }

            // Handle the matched part
            newSegments.add(new TextSegment("", Math.max(segment.start, start),
                    Math.min(segment.end, end), styleClass));

            // Handle segment after match
            if (segment.end > end) {
                newSegments.add(new TextSegment("", end, segment.end, segment.styleClass));
            }
        }

        segments.clear();
        segments.addAll(newSegments);
    }

    /**
     * Sets up hover effect for a line container.
     */
    private void setupHoverEffect(HBox container) {
        // Use a final reference to the container for the lambda expressions
        final HBox finalContainer = container;

        container.setOnMouseEntered(e -> {
            // Check if this container is the currently highlighted one
            int index = lineContainers.indexOf(finalContainer);
            if (index != highlightedLineIndex) {
                finalContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.05);");
            }
        });

        container.setOnMouseExited(e -> {
            // Check if this container is the currently highlighted one
            int index = lineContainers.indexOf(finalContainer);
            if (index != highlightedLineIndex) {
                finalContainer.setStyle("");
            }
        });
    }

    /**
     * Highlights a specific line of pseudocode with animation.
     *
     * @param index The zero-based index of the line to highlight
     */
    public void highlightLine(int index) {
        // Make the index final for use in the lambda
        final int lineIndex = index;

        Platform.runLater(() -> {
            // Stop any ongoing animation
            if (blinkAnimation != null) {
                blinkAnimation.stop();
            }

            // Clear previous highlight
            clearHighlightInternal();

            // Store previous line for tracking execution flow
            if (highlightedLineIndex != -1) {
                previousHighlightedLine = highlightedLineIndex;
                markExecutedLine(previousHighlightedLine);
            }

            // Set new highlight
            highlightedLineIndex = lineIndex;
            if (lineIndex >= 0 && lineIndex < lineContainers.size()) {
                HBox container = lineContainers.get(lineIndex);

                // Create fade-in transition
                FadeTransition fadeIn = new FadeTransition(HIGHLIGHT_TRANSITION_DURATION, container);
                fadeIn.setFromValue(0.7);
                fadeIn.setToValue(1.0);

                // Apply highlight style
                container.getStyleClass().add(HIGHLIGHT_CLASS);

                // Scroll to make highlighted line visible
                scrollToLine(lineIndex);

                // Play transition
                fadeIn.play();

                // Start blink animation
                blinkAnimation.play();
            }
        });
    }

    /**
     * Marks a line as executed by changing its background color.
     */
    private void markExecutedLine(int index) {
        if (index >= 0 && index < lineContainers.size()) {
            HBox container = lineContainers.get(index);
            container.getStyleClass().remove(HIGHLIGHT_CLASS);
            container.getStyleClass().add(EXECUTED_LINE_CLASS);
        }
    }

    /**
     * Clears the current line highlight (internal implementation).
     */
    private void clearHighlightInternal() {
        if (highlightedLineIndex != -1 && highlightedLineIndex < lineContainers.size()) {
            HBox container = lineContainers.get(highlightedLineIndex);
            container.getStyleClass().remove(HIGHLIGHT_CLASS);
            container.setStyle("");
        }
    }

    /**
     * Clears the current line highlight (public API).
     */
    public void clearHighlight() {
        Platform.runLater(this::clearHighlightInternal);
    }

    /**
     * Clears all highlights and execution markers.
     */
    public void clearAllHighlights() {
        Platform.runLater(() -> {
            for (HBox container : lineContainers) {
                container.getStyleClass().remove(HIGHLIGHT_CLASS);
                container.getStyleClass().remove(EXECUTED_LINE_CLASS);
                container.setStyle("");
            }
            highlightedLineIndex = -1;
            previousHighlightedLine = -1;

            if (blinkAnimation != null) {
                blinkAnimation.stop();
            }
        });
    }

    /**
     * Scrolls the view to make a specific line visible.
     */
    private void scrollToLine(int lineIndex) {
        if (lineIndex >= 0 && lineIndex < lineContainers.size()) {
            // Calculate position in scrollpane (approximate since actual heights may vary)
            double position = (double) lineIndex / Math.max(1, lineContainers.size() - 1);

            // Adjust to center the line if possible
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            double lineHeight = lineContainers.get(0).getHeight();
            double contentHeight = lineContainers.size() * lineHeight;

            if (contentHeight > viewportHeight) {
                double linePosition = lineIndex * lineHeight;
                double adjustment = (viewportHeight / 2 - lineHeight / 2) / contentHeight;
                position = Math.max(0, Math.min(1, (linePosition - adjustment * contentHeight) / contentHeight));
            }

            // Set scroll position
            final double finalPosition = position;
            scrollPane.setVvalue(finalPosition);
        }
    }

    /**
     * Gets the main container pane.
     *
     * @return The VBox containing all components
     */
    public VBox getPane() {
        return pane;
    }
}