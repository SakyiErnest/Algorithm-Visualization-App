/* File: MainApp.java */
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainController mainController = new MainController();

        // Wrap the entire UI in a ScrollPane
        ScrollPane rootScrollPane = new ScrollPane(mainController.getRoot());
        rootScrollPane.setFitToWidth(true);
        rootScrollPane.setFitToHeight(true);

        Scene scene = new Scene(rootScrollPane, 900, 700);
        // Apply our modern styling theme
        scene.getStylesheets().add("data:text/css," + MainController.MODERN_STYLE.replaceAll("\n", ""));
        primaryStage.setTitle("Algorithm Visualizer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(850);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
