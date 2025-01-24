package be.ucll.da.postservice.domain.post.sagas;

import be.ucll.da.postservice.adapters.messaging.RabbitEventSender;
import be.ucll.da.postservice.client.user.model.PostLikeUserValidatedEvent;
import be.ucll.da.postservice.domain.post.Post;
import be.ucll.da.postservice.domain.post.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LikePostSaga {
    private final static Logger LOGGER = LoggerFactory.getLogger(LikePostSaga.class);
    private final PostRepository postRepository;
    private final RabbitEventSender eventSender;

    @Autowired
    public LikePostSaga(PostRepository postRepository, RabbitEventSender eventSender) {
        this.postRepository = postRepository;
        this.eventSender = eventSender;
    }

    public void executeSaga(Integer postId, Integer userId) {
        eventSender.sendValidatePostLikeUser(postId, userId);
    }

    public void executeSaga(Integer postId, PostLikeUserValidatedEvent event) {
        Post post = postRepository.findById(postId);
        if (event.getIsValid()) {
            post.addLike(event.getUserId());

            eventSender.sendPostUpdatedEvent(post); // for CQRS in feed-service
            eventSender.sendEmail(post.getOwnerEmail(), "Your Post with id " + postId + " is liked by user " + event.getEmail());

        } else {
            LOGGER.warn("User does not exist.");
        }
    }
}
