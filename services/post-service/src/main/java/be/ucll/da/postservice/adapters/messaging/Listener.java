package be.ucll.da.postservice.adapters.messaging;

import be.ucll.da.postservice.client.user.model.OwnerValidatedEvent;
import be.ucll.da.postservice.client.user.model.PostCommentUserValidatedEvent;
import be.ucll.da.postservice.client.user.model.PostLikeUserValidatedEvent;
import be.ucll.da.postservice.client.user.model.TaggedUsersValidatedEvent;
import be.ucll.da.postservice.domain.post.sagas.CreatePostSaga;
import be.ucll.da.postservice.domain.post.sagas.LikePostSaga;
import be.ucll.da.postservice.domain.post.sagas.CommentOnPostSaga;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class Listener {
    private final static Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final CreatePostSaga saga;
    private final LikePostSaga likeSaga;
    private final CommentOnPostSaga commentOnSaga;

    @Autowired
    public Listener(CreatePostSaga saga, LikePostSaga likeSaga, CommentOnPostSaga commentOnSaga) {
        this.saga = saga;
        this.likeSaga = likeSaga;
        this.commentOnSaga = commentOnSaga;
    }

    @RabbitListener(queues = {"q.owner-validated.post-service"})
    public void onOwnerValidated(OwnerValidatedEvent event) {
        LOGGER.info("Receiving event: " + event);
        this.saga.executeSaga(event.getPostId(), event);
    }

    @RabbitListener(queues = {"q.tagged-user-validated.post-service"})
    public void onTaggedValidated(TaggedUsersValidatedEvent event) {
        LOGGER.info("Receiving event: " + event);
        this.saga.executeSaga(event.getPostId(), event);
    }

    @RabbitListener(queues = {"q.user-like-validated.post-service"})
    public void onPostLikeUserValidated(PostLikeUserValidatedEvent event) {
        LOGGER.info("Receiving event: " + event);
        this.likeSaga.executeSaga(event.getPostId(), event);
    }

    @RabbitListener(queues = {"q.user-comment-validated.post-service"})
    public void onPostCommentUserValidated(PostCommentUserValidatedEvent event) {
        LOGGER.info("Receiving event: " + event);
        this.commentOnSaga.execute(event.getPostId(), event);
    }
}
