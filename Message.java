package task3008;

import java.io.Serializable;

/**
 * Created by ОСО on 04.04.17.
 */
public class Message implements Serializable {
    final private MessageType type;
    final private String data;

    public Message(MessageType type) {
        this.type = type;
        this.data = null;
    }

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}
