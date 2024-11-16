import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String type;
    private String status;
    private String text;
    private String username; // New field for the sender's username

    public Message(String type) {
        if (!type.equals("login") && !type.equals("text") && !type.equals("logout")) {
            throw new IllegalArgumentException("Invalid message type");
        }
        this.type = type;
        this.status = "";
        this.text = "";
        this.username = "";
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
