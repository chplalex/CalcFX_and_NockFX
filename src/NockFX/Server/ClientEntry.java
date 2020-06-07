package NockFX.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static NockFX.Const.*;

public class ClientEntry {
    private Controller controller;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;

    public ClientEntry(Controller controller, Socket socket) {

        this.controller = controller;
        this.socket = socket;

        try {

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // цикл аутентификации
                    do {

                        String msg = in.readUTF();
                        String[] msgArr = new String[3];
                        msgArr = msg.split(" ", 3);

                        if (msgArr[0].equals(CMD_STOP)) {
                            out.writeUTF(CMD_STOP);
                            return;
                        }

                        if (msgArr.length == 3 || msgArr[0].equals(CMD_AUTH)) {
                            nick = controller.authService.getNickByLogAndPass(msgArr[1], msgArr[2]);
                        }

                    } while (nick == null);

                    out.writeUTF(CMD_AUTH_OK + " " + nick);
                    controller.putText(nick + " :: авторизован");

                    // цикл работы
                    while (true) {
                        String msg = in.readUTF();
                        controller.putText(nick + " :: " + msg);
                        if (msg.equals(CMD_STOP)) {
                            controller.putText(nick + " :: отключаю");
                            break;
                        }

                        String[] msgArr = msg.trim().split("\\s*(\\s)\\s*", 3);

                        if (msgArr.length == 3 && msgArr[0].equalsIgnoreCase(CMD_PRIVATE_MSG)) {
                            controller.privateMsg(nick, msgArr[1], msgArr[2]);
                            continue;
                        }

                        controller.broadcastMsg(nick + " :: " + msg);
                    }
                } catch (IOException e) {
                    controller.putText("Проблема связи с клиентом " + socket.toString() + " " + e.toString());
                } finally {
                    controller.removeClient(this);
                    closeConnection();
                }
            }).start();

        } catch (IOException e) {
            controller.putText("Ошибка подключения клиента " + e.toString());
        }
    }

    public String getNick() {
        return nick;
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            controller.putText("Ошибка отправки сообщения клиенту " + e.toString());
        }
    }

    public void closeConnection() {
        controller.putText("Удаляю клиента " + socket.toString());
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            controller.putText("Ошибка закрытия потоков " + e.toString());
        }
        nick = null;
    }

}
