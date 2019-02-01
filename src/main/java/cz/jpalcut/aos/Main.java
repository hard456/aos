package cz.jpalcut.aos;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage window;

    @Override
    public void start(Stage primaryStage) throws Exception{
        window = primaryStage;
        FXMLLoader loader =  new FXMLLoader(getClass().getResource("/gui.fxml"));
        Parent root = loader.load();
        window.setTitle("AOS - inverzní filtr");
        window.setScene(new Scene(root, 1024, 768));
        window.show();
    }

    public static Stage getStage(){
        return window;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
