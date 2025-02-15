package application;

public class Answer {
    private int id;
    private String text;
    private String author;
    private boolean isAccepted;

    // ganesh: Constructor
    public Answer(int id, String text, String author) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.isAccepted = false;
    }

    // ganesh: Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public boolean isAccepted() { return isAccepted; }
    
    // ganesh: Mark answer as accepted
    public void acceptAnswer() {
        this.isAccepted = true;
    }
}
