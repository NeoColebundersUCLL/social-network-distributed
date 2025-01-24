package be.ucll.da.feedservice.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Comment {

    private int id;
    private int userId;
    private String content;

    public Comment() {}

    public Comment(int id, int userId, String content) {
        this.id = id;
        this.userId = userId;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public void setId(int id) {
        this.id = id;
    }
}