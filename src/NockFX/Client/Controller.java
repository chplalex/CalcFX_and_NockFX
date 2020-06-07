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
        } catch (IOException e) {
            putText("Отсутствует подключение к серверу");
        }

        new Thread(() -> {
            try {
                while (true) {
                    String msg = in.readUTF().trim();

                    // Сервер отклоняет аутентификацию
                    if (msg.startsWith(CMD_AUTH_NO)) {
                        putText("Вы не авторизованы в чате");
                        clientNick = null;
                        setFieldsVisibility(false);
                        continue;
                    }

                    // Сервер подтверждает аутентификацию
                    if (msg.startsWith(CMD_AUTH_OK)) {
                        String[] msgArr = msg.split(CMD_REGEX, 2);
                        if (msgArr.length != 2) {
                            putText("Некорректная команда от сервера :: " + msg);
                            clientNick = null;
                            setFieldsVisibility(false);
                            continue;
                        }
                        clientNick = msgArr[1];
                        setFieldsVisibility(true);
                        putText("Вы вошли в чат под ником " + clientNick);
                    }

                    // Сервер прислал широковещательное сообщение
                    if (msg.startsWith(CMD_BROADCAST_MSG)) {
                        String[] msgArr = msg.split(CMD_REGEX, 3);
                        if (msgArr.length != 3) {
                            putText("Некорректная команда от сервера :: " + msg);
                            continue;
                        }
                        putText(msgArr[1] + " -> (всем) :: " + msgArr[2]);
                        continue;
                    }

                    // Сервер прислал приватное сообщение
                    if (msg.startsWith(CMD_PRIVATE_MSG)) {
                        String[] msgArr = msg.split(CMD_REGEX, 3);
                        if (msgArr.length != 3) {
                            putText("Некорректная команда от сервера :: " + msg);
                            continue;
                        }
                        putText(msgArr[1] + " -> (только мне) :: " + msgArr[2]);
                        continue;
                    }

                    // Сервер даёт команду на закрытие клиента
                    if (msg.startsWith(CMD_STOP_CLIENT)) {
                        clientConnected = false;
                        putText("Соединение с сервером остановлено");
                        break;
                    }
                }
            } catch (IOException e) {
                putText("Ошибка чтения сообщения " + e.toString());
            } finally {
                close();
            }
        }).start();
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
                out.writeUTF(CMD_STOP_CLIENT);
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
        if (!clientConnected) {
            putText("Подключение к серверу не установлено");
            return;
        }

        String msg = textField.getText().trim();

        if (msg.startsWith(CMD_STOP_CLIENT) ||
                msg.startsWith(CMD_AUTH) ||
                msg.startsWith(CMD_BROADCAST_MSG) ||
                msg.startsWith(CMD_PRIVATE_MSG)) {
            putText("Служебные символы в начале сообщения недопустимы");
            return;
        }

        try {
            if (msg.startsWith(USER_PRIVATE_MSG)) {
                String[] msgArr = msg.split(CMD_REGEX, 3);
                if (msgArr.length != 3) {
                    putText("Используйте корректный формат команды:\n/w <кому> <сообщение>");
                } else {
                    out.writeUTF(CMD_PRIVATE_MSG + " " + msgArr[1] + " " + msgArr[2]);
                    textField.setText("");
                }
                return;
            }

            if (msg.startsWith(USER_DE_AUTH)) {
                out.writeUTF(CMD_DE_AUTH);
                textField.setText("");
                return;
            }

            out.writeUTF(CMD_BROADCAST_MSG + " " + msg);
            textField.setText("");
        } catch (IOException e) {
            putText("Ошибка отправки сообщения " + e.toString());
        }
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
