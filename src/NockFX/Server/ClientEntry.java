package NockFX.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static NockFX.Const.CMD_STOP;

public class ClientEntry {
    Controller controller;
    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public ClientEntry(Controller controller, Socket socket) {

        this.controller = controller;
        this.socket = socket;

        try {

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    while (true) {
                        String msg = in.readUTF();
                        controller.putText(msg + " " + socket.toString());
                        if (msg.equalsIgnoreCase(CMD_STOP)) {
                            controller.putText("Отключаю клиента " + socket.toString());
                            sendMsg(CMD_STOP);
                            break;
                        }
                        controller.broadcastMsg(msg);
                    }
                } catch (IOException e) {
                    controller.putText("Проблема связи с клиентом " + socket.toString() + " " + e.toString());
                } finally {
                    remove();
                }
            }).start();

        } catch (IOException e) {
            controller.putText("Ошибка подключения клиента " + e.toString());
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            controller.putText("Ошибка отправки сообщения клиенту " + e.toString());
        }
    }

    public void remove() {
        controller.putText("Удаляю клиента " + socket.toString());
        controller.removeClient(this);
    }

}
