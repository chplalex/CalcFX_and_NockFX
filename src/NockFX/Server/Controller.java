package NockFX.Server;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import static NockFX.Const.CMD_STOP;
import static NockFX.Const.SERVER_PORT;

public class Controller implements Initializable {

    List<ClientEntry> clients;
    AuthService authService;
    private ServerSocket serverSocket;
    private boolean serverRunning;

    @FXML
    private TextArea textArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        clients = new Vector<>();
        authService = new TestAuthService();
        serverRunning = false;

        try {

            serverSocket = new ServerSocket(SERVER_PORT);
            serverRunning = true;
            putText("Сервер запущен. " + serverSocket.toString());

            new Thread(() -> {
                while (serverRunning) {
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
                        putText("Клиент подключён. " + socket.toString());
                        clients.add(new ClientEntry(this, socket));
                    } catch (IOException e) {
                        if (serverRunning) {
                            putText("Ошибка сервера. " + e.toString());
                        } else {
                            putText("Сервер закрыт. " + e.toString());
                        }
                        break;
                    }
                }
            }).start();

        } catch (IOException e) {
            putText("Ошибка запуска сервера. " + e.toString());
        }
    }

    public void close() {
        serverRunning = false;
        System.out.println("clients.size = " + clients.size());
        for (ClientEntry clientEntry: clients) {
            clientEntry.sendMsg(CMD_STOP);
            clientEntry.closeConnection();
        }
        clients.clear();
        try {
            serverSocket.close();
        } catch (IOException e) {
            putText("Ошибка закрытия сервера. " + e.toString());
        }
    }

    public void putText(String text) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        textArea.appendText(dateFormat.format(new Date()) + "\n" + text + "\n\n");
    }

    public void broadcastMsg(String msg) {
        for (ClientEntry clientEntry: clients) {
            clientEntry.sendMsg(msg);
        }
    }

    public void privateMsg(String sender, String recipient, String msg) {
        for (ClientEntry clientEntry: clients) {
            if (recipient.equals(clientEntry.getNick())) {
                clientEntry.sendMsg(sender + " (только мне) :: " + msg);
            }
        }
    }

    public void removeClient(ClientEntry clientEntry) {
        clients.remove(clientEntry);
    }

}