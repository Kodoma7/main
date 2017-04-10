package task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ОСО on 04.04.17.
 */
public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите порт сервера");
        int port = ConsoleHelper.readInt();
        try (ServerSocket server = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Запуск сервера");

            while (true) {
                Socket socket = server.accept();
                new Handler(socket).start();
            }
        } catch (Exception e) {
            ConsoleHelper.writeMessage(e.getMessage());
        }
    }

    public static void sendBroadcastMessage(Message message) {
        Iterator<Map.Entry<String, Connection>> iterator = connectionMap.entrySet().iterator();
        while (iterator.hasNext()) {
           Connection connection = iterator.next().getValue();
            try {
                connection.send(message);
            } catch (IOException e) {
                try {
                    connection.send(new Message(MessageType.TEXT, "При отправке сообщения возникла ошибка"));
                } catch (IOException e1) {
                    ConsoleHelper.writeMessage(e1.getMessage());
                }
            }
        }
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String name = null;
            ConsoleHelper.writeMessage("Установлено соединение с " + socket.getRemoteSocketAddress());

            try (Connection connection = new Connection(socket)) {
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                sendListOfUsers(connection, name);
                serverMainLoop(connection, name);

            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным сервером");
            }
            finally {
                if (name != null) {
                    connectionMap.remove(name);
                    sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
                }
            }
            ConsoleHelper.writeMessage("Cоединение с адресом " + socket.getRemoteSocketAddress() + " закрыто.");
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String name = null;
            boolean ready = false;

            while (!ready) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();

                if (message.getType() == MessageType.USER_NAME) {
                    name = message.getData();
                    if (!name.isEmpty() && connectionMap.get(name) == null) {
                        connectionMap.put(name, connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        ready = true;
                    }
                }
            }
            return name;
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException {
            Iterator<Map.Entry<String, Connection>> iterator = connectionMap.entrySet().iterator();

            while (iterator.hasNext()) {
                String name = iterator.next().getKey();
                if (!name.equals(userName)) connection.send(new Message(MessageType.USER_ADDED, name));
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == MessageType.TEXT) {
                    String text = userName + ": " + message.getData();
                    sendBroadcastMessage(new Message(MessageType.TEXT, text));

                } else ConsoleHelper.writeMessage("Ошибка");
            }
        }

    }
}
