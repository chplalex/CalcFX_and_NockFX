package NockFX.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import static NockFX.Const.*;

public class Controller implements Initializable {

    private Stage stage;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean clientRunning;

    @FXML
    private TextArea textArea;

    @FXML
    private TextField textField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        clientRunning = false;

        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            clientRunning = true;
            putText("Клиент подключен к серверу " + socket.toString());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (clientRunning) {
                            String msg = in.readUTF();
                            if (msg.equalsIgnoreCase(CMD_STOP)) {
                                clientRunning = false;
                                putText("Клиент отключен от сервера.");
                                break;
                            }
                            putText(msg);
                        }
                    } catch (IOException e) {
                        putText("Ошибка чтения сервера " + e.toString());
                    } finally {
                        close();
                    }
                }
            }).start();
        } catch (IOException e) {
            putText("Ошибка подключения к серверу " + e.toString());
        }

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void close() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            putText("Ошибка закрытия клиента. " + e.toString());
        }
    }

    public void putText(String text) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        textArea.appendText(dateFormat.format(new Date()) + "\n" + text + "\n");
    }

    public void sendMsg(ActionEvent actionEvent) {
        if (clientRunning) {
            try {
                out.writeUTF(textField.getText());
                textField.setText("");
            } catch (IOException e) {
                putText("Ошибка отправки сообщения " + e.toString());
            }
        } else {
            putText("Клиент не подключен к серверу.");
        }
        textField.setText("");
    }

}
