package be.ucll.da.feedservice.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Post {

    @Id
    private int id;

    private int ownerId;

    private String ownerEmail;

    private String body;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    private List<Integer> tags = new ArrayList<>();

    @Column(nullable = false)
    private int likeCount = 0;

    @ElementCollection
    private Set<Integer> likesInUserIds = new HashSet<>();

    @ElementCollection
    private List<Comment> comments = new ArrayList<>();

    public Post() {
    }

    public Post(int id, int ownerId, String ownerEmail, String body, List<Integer> tags, int likeCount) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerEmail = ownerEmail;
        this.body = body;
        this.tags = tags != null ? tags : new ArrayList<>();
        this.likeCount = likeCount;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public int getId() {
        return id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public List<Integer> getTags() {
        return tags;
    }

    public String getBody() {
        return body;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public Set<Integer> getLikesInUserIds() {
        return likesInUserIds;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void removeLike(int userId) {
        if (likesInUserIds.remove(userId)) {
            likeCount--;
        }
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setTags(List<Integer> tags) {
        this.tags = tags;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }
}
