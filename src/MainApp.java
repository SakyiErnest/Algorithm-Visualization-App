/* File: MainApp.java */
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private MainController mainController;
    
    @Override
    public void start(Stage primaryStage) {
        mainController = new MainController();

        // Wrap the entire UI in a ScrollPane
        ScrollPane rootScrollPane = new ScrollPane(mainController.getRoot());
        rootScrollPane.setFitToWidth(true);
        rootScrollPane.setFitToHeight(true);

        Scene scene = new Scene(rootScrollPane, 900, 700);
        // Load CSS stylesheet from external file (Corrected approach)
        scene.getStylesheets().add(getClass().getResource("main-controller.css").toExternalForm());
        primaryStage.setTitle("Algorithm Visualizer");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(850);
        primaryStage.setMinHeight(650);
        primaryStage.show();
        
        // Clean up resources when application closes
        primaryStage.setOnCloseRequest(e -> {
            if (mainController != null) {
                mainController.shutdown();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}