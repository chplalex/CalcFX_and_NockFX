package CalcFX;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class CalcFXApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
//        FXMLLoader loader = new FXMLLoader();
//        URL xmlUrl = getClass().getResource("CalcFX.fxml");
//        loader.setLocation(xmlUrl);
//        Parent root = loader.load();
        Parent root = FXMLLoader.load(getClass().getResource("CalcFX.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("The Simplest CalcFX");
//        scene.getStylesheets().add((getClass().getResource("CalcFX.css")).toExternalForm());
        stage.show();
    }
}
