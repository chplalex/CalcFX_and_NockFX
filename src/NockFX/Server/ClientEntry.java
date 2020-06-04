package NockFX.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
                        System.out.println("жду сообщения от клиента: " + socket.toString());
                        String msg = in.readUTF();
                        System.out.println("получил сообщение: " + msg);
                        controller.putText(msg + " " + socket.toString());
                        if (msg.equalsIgnoreCase("/end")) {
                            controller.putText("Отключился клиент " + socket.toString());
                            break;
                        }
                        controller.broadcastMsg(msg);
                    }
                } catch (IOException e) {
                    controller.putText("Проблема связи с клиентом " + socket.toString() + " " + e.toString());
                } finally {
                    closeAndRemove();
                }
            }).start();

        } catch (IOException e) {
            controller.putText("ClientEntry 49 " + e.toString());
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            controller.putText("ClientEntry 59 " + e.toString());
        }
    }

    public void closeAndRemove() {
        try {
            controller.putText("Удаляю клиента " + socket.toString());
            controller.removeClient(this);
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            controller.putText("Проблема с закрытием ресурсов клиента " + socket.toString() + " " + e.toString());
        }
    }


}
