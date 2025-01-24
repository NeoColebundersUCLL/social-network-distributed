package be.ucll.da.postservice.domain.post.sagas;

import be.ucll.da.postservice.adapters.messaging.RabbitEventSender;
import be.ucll.da.postservice.client.user.model.OwnerValidatedEvent;
import be.ucll.da.postservice.client.user.model.TaggedUsersValidatedEvent;
import be.ucll.da.postservice.domain.post.Post;
import be.ucll.da.postservice.domain.post.PostRepository;
import be.ucll.da.postservice.domain.post.PostStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreatePostSaga {

    private final static Logger LOGGER = LoggerFactory.getLogger(CreatePostSaga.class);
    private final RabbitEventSender eventSender;
    private final PostRepository postRepository;

    @Autowired
    public CreatePostSaga(RabbitEventSender eventSender, PostRepository postRepository) {
        this.eventSender = eventSender;
        this.postRepository = postRepository;
    }

    public void executeSaga(Post post) {
        post.validatingOwner();
        eventSender.sendValidateOwnerCommand(post.getId(), post.getOwnerId());
    }

    public void executeSaga(Integer postId, OwnerValidatedEvent event) {
        Post post = postRepository.findById(postId);
        if (event.getIsOwner()) {
            post.validatingTaggedUsers(event.getEmail());
            eventSender.sendValidateTaggedUsersCommand(postId, post.getTags());
        } else {
            post.ownerInvalid();
            LOGGER.warn("Post cannot be created, owner is invalid for postId {}", postId);

            rollbackPostCreation(post);
        }
    }

    public void executeSaga(Integer postId, TaggedUsersValidatedEvent event) {
        Post post = postRepository.findById(postId);

        if (event.getTaggedUsersValid()) {

            List<Integer> userIds = new ArrayList<>();
            event.getTags().forEach(apiUser -> {
                userIds.add(apiUser.getId());
                eventSender.sendEmail(apiUser.getEmail(), generateMessage(post.getOwnerEmail()));
            });

            post.taggedUsersValid(userIds);
            eventSender.sendPostCreatedEvent(post); // for CQRS in feedService

        } else {
            post.taggedUsersInvalid();
            eventSender.sendEmail(
                    post.getOwnerEmail(), "Post creation failed, tagged users are invalid, rollback initiated"
            );
            rollbackPostCreation(post);
        }
    }

    // helper functions
    private void rollbackPostCreation(Post post) {
        if (post.getStatus() != PostStatus.ACCEPTED) {
            postRepository.delete(post);
        }
    }

    private String generateMessage(String email) {
        return "You are tagged in a post of " + email + ": ";
    }
}
