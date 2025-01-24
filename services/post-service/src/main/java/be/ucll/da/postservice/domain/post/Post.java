package be.ucll.da.postservice.domain.post;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int ownerId;

    private boolean isValidOwnerId;

    private String ownerEmail;

    private String body;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    private List<Integer> tags = new ArrayList<>() ;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @Column(nullable = false)
    private int likeCount = 0;

    @ElementCollection
    private Set<Integer> likesInUserIds = new HashSet<>();

    @ElementCollection
    private List<Comment> comments = new ArrayList<>();

    public Post() {}

    public Post(int ownerId, String body, List<Integer> tags) {
        this.ownerId = ownerId;
        this.body = body;
        this.status = PostStatus.REGISTERED;
        this.tags = tags != null ? tags : new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public String getBody() {
        return body;
    }

    public List<Integer> getTags() {
        return tags;
    }

    public void setTags(List<Integer> tags) {
        this.tags = tags;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public boolean getIsValidOwnerId() {
        return isValidOwnerId;
    }

    public PostStatus getStatus() {
        return status;
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

    public void addLike(int userId) {
        if (likesInUserIds.add(userId)) {
            likeCount++;
        }
    }

    public void removeLike(int userId) {
        if (likesInUserIds.remove(userId)) {
            likeCount--;
        }
    }

    public void addComment(Comment comment) {
        int newId = comments.stream()
                .mapToInt(Comment::getId)
                .max()
                .orElse(0) + 1;
        comment.setId(newId);
        comments.add(comment);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
    }

    public void taggedUsersValid(List<Integer> userIds) {
        this.status = PostStatus.ACCEPTED;
        for (Integer userId : userIds) {
            if (!tags.contains(userId)) {
                tags.add(userId);
            }
        }
    }

    public void taggedUsersInvalid() {
        this.status = PostStatus.USERS_NOT_VALID;
    }

    public void validatingOwner() {
        this.status = PostStatus.VALIDATING_OWNER;
    }

    public void ownerInvalid() {
        this.status = PostStatus.NO_OWNER;
    }

    public void validatingTaggedUsers(String ownerEmail) {
        this.status = PostStatus.VALIDATING_TAGGED_USERS;
        this.isValidOwnerId = true;
        this.ownerEmail = ownerEmail;
    }
}
