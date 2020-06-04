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

public class Controller implements Initializable {

    List<ClientEntry> clients;
    private final int PORT = 8189;
    private ServerSocket serverSocket;
    private boolean serverRunning;

    @FXML
    private TextArea textArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        clients = new Vector<>();
        serverRunning = false;

        try {

            serverSocket = new ServerSocket(PORT);
            serverRunning = true;
            putText("Сервер запущен. " + serverSocket.toString());

            new Thread(() -> {
                while (serverRunning) {
                    Socket socket = null;
                    try {
                        socket = serverSocket.accept();
                    } catch (IOException e) {
                        if (serverRunning) {
                            putText("Ошибка сервера. " + e.toString());
                        } else {
                            putText("Сервер закрыт. " + e.toString());
                        }
                        break;
                    }
                    putText("Клиент подключён. " + socket.toString());
                    clients.add(new ClientEntry(this, socket));
                }
            }).start();

        } catch (IOException e) {
            putText("Ошибка запуска сервера. " + e.toString());
        }
    }

    public void close() {
        serverRunning = false;
        for (ClientEntry clientEntry: clients) {
            clientEntry.closeAndRemove();
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            putText("Ошибка закрытия сервера. " + e.toString());
        }
    }

    public void putText(String text) {
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        textArea.appendText(dateFormat.format(new Date()) + " " + text + "\n");
    }

    public void broadcastMsg(String msg) {
        for (ClientEntry clientEntry: clients) {
            clientEntry.sendMsg(msg);
        }
    }

    public void removeClient(ClientEntry clientEntry) {
        clients.remove(clientEntry);
    }
}