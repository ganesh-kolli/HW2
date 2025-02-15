package application;

import java.util.ArrayList;

public class Question {
    private int id;
    private String text;
    private String author;
    private ArrayList<Answer> answers; // Stores possible answers

    // ganesh: Constructor
    public Question(int id, String text, String author) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.answers = new ArrayList<>();
    }

    // ganesh: Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public ArrayList<Answer> getAnswers() { return answers; }
    
    // ganesh: Add an answer to this question
    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    // ganesh: Remove an answer by index
    public void removeAnswer(int index) {
        if (index >= 0 && index < answers.size()) {
            answers.remove(index);
        }
    }
}
