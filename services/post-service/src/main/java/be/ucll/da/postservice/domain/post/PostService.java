package be.ucll.da.postservice.domain.post;

import be.ucll.da.postservice.adapters.messaging.RabbitEventSender;
import be.ucll.da.postservice.api.model.ApiComment;
import be.ucll.da.postservice.api.model.ApiPost;
import be.ucll.da.postservice.domain.post.sagas.CreatePostSaga;
import be.ucll.da.postservice.domain.post.sagas.LikePostSaga;
import be.ucll.da.postservice.domain.post.sagas.CommentOnPostSaga;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
public class PostService {
    private final PostRepository postRepository;

    // saga
    private final CreatePostSaga createPostSaga;
    private final LikePostSaga likePostSaga;
    private final CommentOnPostSaga commentOnPostSaga;
    private final RabbitEventSender sender;

    @Autowired
    public PostService(PostRepository postRepository, CreatePostSaga createPostSaga, LikePostSaga likePostSaga, CommentOnPostSaga commentOnPostSaga, RabbitEventSender sender) {
        this.postRepository = postRepository;
        this.createPostSaga = createPostSaga;
        this.likePostSaga = likePostSaga;
        this.commentOnPostSaga = commentOnPostSaga;
        this.sender = sender;
    }

    public List<Post> getAllPosts() {
        return (List<Post>) postRepository.findAll();
    }

    // create post with saga
    public void createPostRequest(ApiPost postData) {
        var post = new Post(postData.getOwnerId(), postData.getBody(), postData.getTags());
        Post savedPost = postRepository.save(post);
        createPostSaga.executeSaga(savedPost);
    }

    // like post with saga
    public void likePostRequest(int postId, int userId) {
        Post post = postRepository.findById(postId);
        if (post == null) throw new PostException("Post not found");
        if (post.getLikesInUserIds().contains(userId)) throw new PostException("You already liked this post");

        likePostSaga.executeSaga(post.getId(), userId);
    }

    // comment on post with saga
    public void commentOnPost(int postId, ApiComment comment) {
        Post post = postRepository.findById(postId);
        if (post == null) throw new PostException("Post not found");

        commentOnPostSaga.execute(postId, comment);
    }

    // remove post -> sends event to feed-service for CQRS
    public void removePost(int postId, int userId) {
        Post post = postRepository.findById(postId);

        if (post == null) throw new PostException("Post not found");
        if (post.getOwnerId() != userId) throw new PostException("Only owner can delete post");

        postRepository.delete(post);
        sender.sendRemovePostEvent(postId, userId);
    }

    // remove like -> sends event to feed-service for CQRS
    public void removeLike(int postId, int userId) {
        Post post = postRepository.findById(postId);

        if (!post.getLikesInUserIds().contains(userId)) {
            throw new PostException("You did not like this post");
        }

        post.removeLike(userId);
        postRepository.save(post);
        sender.sendRemoveLikeEvent(postId, userId);
    }

    // remove post -> send event to feed-service for CQRS
    public void removeComment(int postId, int commentId, int userId) {
        Post post = postRepository.findById(postId);
        if (post == null) throw new PostException("Post not found");

        Comment commentToRemove = post.getComments().stream()
                .filter(comment -> comment.getId() == commentId)
                .findFirst()
                .orElseThrow(() -> new PostException("Comment not found"));

        if (commentToRemove.getUserId() != userId) {
            throw new PostException("You do not have permission to delete this comment");
        }

        post.removeComment(commentToRemove);
        postRepository.save(post);
        // todo: event to feed-service
        sender.sendRemoveCommentEvent(postId, commentId, userId);
    }
}
