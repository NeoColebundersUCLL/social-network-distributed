package be.ucll.da.userservice.adapters.messaging;

import be.ucll.da.userservice.api.model.*;
import be.ucll.da.userservice.domain.User;
import be.ucll.da.userservice.domain.UserService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Transactional
public class Listener {
    private final static Logger LOGGER = LoggerFactory.getLogger(Listener.class);

    private final UserService userService;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public Listener(UserService userService, RabbitTemplate rabbitTemplate) {
        this.userService = userService;
        this.rabbitTemplate = rabbitTemplate;
    }

    private ApiUser toUserDto(User user) {
        ApiUser apiUser = new ApiUser();
        apiUser.setId(user.getId());
        apiUser.setName(user.getName());
        apiUser.setEmail(user.getEmail());
        return apiUser;
    }

    @RabbitListener(queues = {"q.user-service.validate-tagged-users"})
    public void onValidateTaggedUsers(ValidateTaggedUsersCommand command) {
        LOGGER.info("Received command: " + command);

        List<User> users = userService.getExistingUsers(command.getTags());
        boolean isTaggedUsersValid = users != null
                && !users.contains(null)
                && users.stream().map(User::getId).collect(Collectors.toSet()).containsAll(command.getTags());

        TaggedUsersValidatedEvent event = new TaggedUsersValidatedEvent();
        event.setPostId(command.getPostId());

        if (isTaggedUsersValid) {
            List<ApiUser> apiUsers = users.stream()
                    .filter(Objects::nonNull)
                    .map(this::toUserDto)
                    .collect(Collectors.toList());
            event.setTags(apiUsers);
        } else {
            event.setTags(Collections.emptyList());
        }

        event.setTaggedUsersValid(isTaggedUsersValid);

        LOGGER.info("Sending event: " + event);
        this.rabbitTemplate.convertAndSend("x.tagged-users-validated", "", event);
    }

    @RabbitListener(queues = {"q.user-service.validate-owner"})
    public void onValidateOwner(ValidateOwnerCommand command) {
        LOGGER.info("Received command: " + command);

        User owner = userService.validateUser(command.getUserId());

        OwnerValidatedEvent event = new OwnerValidatedEvent();
        event.setPostId(command.getPostId());

        if (owner != null) {
            event.setUserId(owner.getId());
            event.setName(owner.getName());
            event.setEmail(owner.getEmail());
            event.setIsOwner(true);
        } else {
            event.setIsOwner(false);
        }

        LOGGER.info("Sending event: " + event);
        this.rabbitTemplate.convertAndSend("x.owner-validated", "", event);
    }

    @RabbitListener(queues = {"q.user-service.validate-post-like-user"})
    public void onValidatePostLikeUser(ValidatePostLikeUserCommand command) {
        LOGGER.info("Received command: " + command);

        User user = userService.validateUser(command.getUserId());

        PostLikeUserValidatedEvent event = new PostLikeUserValidatedEvent();
        event.setPostId(command.getPostId());
        if (user != null) {
            event.setUserId(user.getId());
            event.setEmail(user.getEmail());
            event.setIsValid(true);
        } else {
            event.setIsValid(false);
        }

        LOGGER.info("Sending event: " + event);
        this.rabbitTemplate.convertAndSend("x.user-like-validated", "", event);
    }

    @RabbitListener(queues = {"q.user-service.validate-post-comment-user"})
    public void onValidatePostCommentUser(ValidatePostCommentUserCommand command) {
        LOGGER.info("Received command: " + command);

        User user = userService.validateUser(command.getUserId());

        PostCommentUserValidatedEvent event = new PostCommentUserValidatedEvent();
        event.setPostId(command.getPostId());
        if (user != null) {
            event.setUserId(user.getId());
            event.setEmail(user.getEmail());
            event.setIsValid(true);
            event.setContent(command.getContent());
        } else {
            event.setIsValid(false);
        }

        LOGGER.info("Sending event: " + event);
        this.rabbitTemplate.convertAndSend("x.user-comment-validated", "", event);
    }

}
