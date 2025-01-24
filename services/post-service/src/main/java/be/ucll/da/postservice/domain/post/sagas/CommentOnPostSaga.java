package be.ucll.da.postservice.domain.post.sagas;

import be.ucll.da.postservice.adapters.messaging.RabbitEventSender;
import be.ucll.da.postservice.api.model.ApiComment;
import be.ucll.da.postservice.client.user.model.PostCommentUserValidatedEvent;
import be.ucll.da.postservice.domain.post.Comment;
import be.ucll.da.postservice.domain.post.Post;
import be.ucll.da.postservice.domain.post.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CommentOnPostSaga {

    private final static Logger LOGGER = LoggerFactory.getLogger(LikePostSaga.class);
    private final RabbitEventSender sender;
    private final PostRepository postRepository;

    @Autowired
    public CommentOnPostSaga(RabbitEventSender sender, PostRepository postRepository) {
        this.sender = sender;
        this.postRepository = postRepository;
    }

    public void execute(Integer postId, ApiComment comment) {
        sender.sendValidatePostCommentUser(postId, comment.getUserId(), comment.getContent());
    }

    public void execute(Integer postId, PostCommentUserValidatedEvent event) {
        Post post = postRepository.findById(postId);
        if (event.getIsValid()) {
            post.addComment(new Comment(event.getUserId(), event.getContent()));

            sender.sendEmail(
                    post.getOwnerEmail(), event.getEmail() + " has commented on your post with id: " + post.getId()
            );
            sender.sendPostUpdatedEvent(post);
        } else {
            LOGGER.warn("User does not exist.");
        }
    }
}
