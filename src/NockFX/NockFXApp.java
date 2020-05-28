package NockFX;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Dimension;
import java.awt.Toolkit;

public class NockFXApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        final double SPACING = 3;
        final double WIDTH_PROPORTION = 0.25;
        final double HEIGHT_PROPORTION = 0.85;

        VBox root = new VBox(SPACING);
        root.setAlignment(Pos.TOP_CENTER);

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        double stageWidth = d.width * WIDTH_PROPORTION;
        double stageHeight = d.height * HEIGHT_PROPORTION;
        stage.setWidth(stageWidth);
        stage.setHeight(stageHeight);
        stage.setTitle("NockFX messenger");

        Scene scene = new Scene(root);
        stage.setScene(scene);

        ObservableList nodeList = root.getChildren();

        TextArea textOut = new TextArea();
        textOut.setEditable(false);
        textOut.setFocusTraversable(false);

        TextArea textIn = new TextArea();
        textIn.setEditable(true);
        textIn.setFocusTraversable(true);
        textIn.setOnKeyPressed((event) -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                doTextSend(textIn, textOut);
            }
        });

        Button btnSend = new Button("Send");
        btnSend.setPrefWidth(stageWidth);
        btnSend.setFocusTraversable(false);
        btnSend.setOnAction((actionEvent) -> doTextSend(textIn, textOut));

        textIn.setPrefHeight(btnSend.getHeight() * 2);
        textOut.setPrefHeight(stageHeight - btnSend.getHeight() - textOut.getHeight() - 2 * SPACING);
        stage.setMinHeight(300);

        nodeList.addAll(textOut, textIn, btnSend);

        stage.show();
    }

    private void doTextSend(TextArea textIn, TextArea textOut) {
        textOut.appendText(textIn.getText());
        textIn.clear();
    }

}
