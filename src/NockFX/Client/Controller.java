package NockFX.Client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
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
    private boolean clientConnected;
    private String clientNick;

    @FXML
    private HBox boxLogAndPass;
    @FXML
    private TextField logField;
    @FXML
    private PasswordField passField;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        clientConnected = false;
        setFieldsVisibility(false);
        connect();

    }

    private void connect() {

        try {
            socket = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            clientConnected = true;
            putText("Подключение к серверу установлено");

            new Thread(() -> {

                // блок аутентификации
                try {
                    do {
                        String msg = in.readUTF();

                        if (msg.equals(CMD_STOP)) {
                            clientConnected = false;
                            putText("Соединение с сервером остановлено");
                            close();
                            return;
                        }

                        String[] msgArr = new String[2];

                        msgArr = msg.split(" ", 2);

                        if (msgArr[0].equals(CMD_AUTH_OK)) {
                            clientNick = msgArr[1];
                            setFieldsVisibility(true);
                        }

                        if (msgArr[0].equals(CMD_AUTH_NO)) {
                            clientNick = null;
                            setFieldsVisibility(false);
                        }
                    } while (clientNick == null);

                } catch (IOException e) {
                    putText("Ошибка чтения сообщения " + e.toString());
                    System.out.println("Exeption слушания");
                }

                putText("Вы вошли в чат под ником " + clientNick);

                // блок работы
                try {
                    while (clientConnected) {
                        String msg = in.readUTF();
                        if (msg.equals(CMD_STOP)) {
                            clientConnected = false;
                            putText("Cоединение с сервером остановлено");
                            close();
                            return;
                        }
                        putText(msg);
                    }
                } catch (IOException e) {
                    putText("Ошибка чтения сервера " + e.toString());
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
            if (clientNick != null) {
                clientNick = null;
                setFieldsVisibility(false);
            }
            if (clientConnected) {
                out.writeUTF(CMD_STOP);
                return;
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            putText("Ошибка закрытия клиента. " + e.toString());
        }
    }

    public void putText(String text) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        textArea.appendText(dateFormat.format(new Date()) + "\n" + text + "\n\n");
    }

    public void sendMsg(ActionEvent actionEvent) {
        if (clientConnected) {
            try {
                String msg = textField.getText();
                if (!msg.equals(CMD_STOP)) {
                    out.writeUTF(msg);
                }
            } catch (IOException e) {
                putText("Ошибка отправки сообщения " + e.toString());
            }
        } else {
            putText("Нет подключения к серверу");
        }
        textField.setText("");
    }

    public void makeAuth(ActionEvent actionEvent) {
        if (!clientConnected) {
            connect();
        }

        if (!clientConnected) {
            return;
        }

        String log = logField.getText().trim();
        String pass = passField.getText().trim();

        if (log.equals("") || pass.equals("")) {
            putText("Введите ваши логин и пароль");
            return;
        }

        try {
            out.writeUTF(CMD_AUTH + " " + log + " " + pass);
        } catch (IOException e) {
            putText("Ошибка отправки сообщения " + e.toString());
        }

    }

    private void setFieldsVisibility(boolean clientAuthenticated) {

        boxLogAndPass.setVisible(!clientAuthenticated);
        boxLogAndPass.setManaged(!clientAuthenticated);

        textField.setVisible(clientAuthenticated);
        textField.setManaged(clientAuthenticated);

        if (stage == null) {
            return;
        }

        if (clientAuthenticated) {
            setTitle(TITLE_CLIENT_AUTH_OK + clientNick);
        } else {
            setTitle(TITLE_CLIENT_AUTH_NO);
        }
    }

    private void setTitle(String title) {
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }

}
