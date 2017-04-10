package task3008.client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * Created by ОСО on 07.04.17.
 */
public class BotClient extends Client {
    public static void main(String[] args) {
        new BotClient().run();
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        int rand = (int) (Math.random() * 100);
        return "date_bot_" + rand;
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            BotClient.this.sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            super.processIncomingMessage(message);

            if (message.contains(":")) {
                String name = message.substring(0, message.indexOf(":"));
                String text = message.substring(message.indexOf(":") + 2, message.length());

                String pattern = null;

                switch (text) {
                    case "дата":
                        pattern = "d.MM.YYYY";
                        break;
                    case "день":
                        pattern = "d";
                        break;
                    case "месяц":
                        pattern = "MMMM";
                        break;
                    case "год":
                        pattern = "YYYY";
                        break;
                    case "время":
                        pattern = "H:mm:ss";
                        break;
                    case "час":
                        pattern = "H";
                        break;
                    case "минуты":
                        pattern = "m";
                        break;
                    case "секунды":
                        pattern = "s";
                        break;
                }

                if (pattern != null) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
                    String result = "Информация для " + name + ": " + dateFormat.format(new GregorianCalendar().getTime());
                    sendTextMessage(result);
                }
            }
        }
    }
}
