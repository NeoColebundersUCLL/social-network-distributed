package be.ucll.da.postservice.adapters.messaging;

import be.ucll.da.postservice.api.model.*;
import be.ucll.da.postservice.client.user.model.ValidateOwnerCommand;
import be.ucll.da.postservice.client.user.model.ValidatePostCommentUserCommand;
import be.ucll.da.postservice.client.user.model.ValidatePostLikeUserCommand;
import be.ucll.da.postservice.client.user.model.ValidateTaggedUsersCommand;
import be.ucll.da.postservice.domain.post.Comment;
import be.ucll.da.postservice.domain.post.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RabbitEventSender {

    private final static Logger LOGGER = LoggerFactory.getLogger(RabbitEventSender.class);

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitEventSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendEmail(String recipient, String message) {
        var event = new SendEmailEvent();
        event.setRecipient(recipient);
        event.setMessage(message);
        this.rabbitTemplate.convertAndSend("x.user-tagged", "" ,event);
        LOGGER.info("Email send: " + event);
    }


    // saga user validation
    public void sendValidateOwnerCommand(Integer postId, Integer userId) {
        var command = new ValidateOwnerCommand();
        command.postId(postId);
        command.userId(userId);
        this.rabbitTemplate.convertAndSend("q.user-service.validate-owner", command);
        LOGGER.info("Sending message: " + command);
    }

    public void sendValidateTaggedUsersCommand(Integer postId, List<Integer> tags) {
        var command = new ValidateTaggedUsersCommand();
        command.postId(postId);
        command.tags(tags);
        this.rabbitTemplate.convertAndSend("q.user-service.validate-tagged-users", command);
        LOGGER.info("Sending message: " + command);
    }

    public void sendValidatePostLikeUser(Integer postId, Integer userId) {
        var command = new ValidatePostLikeUserCommand();
        command.postId(postId);
        command.userId(userId);
        this.rabbitTemplate.convertAndSend("q.user-service.validate-post-like-user", command);
        LOGGER.info("Sending message: " + command);
    }

    public void sendValidatePostCommentUser(Integer postId, Integer userId, String content) {
        var command = new ValidatePostCommentUserCommand();
        command.postId(postId);
        command.userId(userId);
        command.content(content);
        this.rabbitTemplate.convertAndSend("q.user-service.validate-post-comment-user", command);
        LOGGER.info("Sending message: " + command);
    }

    // for CQRS in feed-service
    public void sendPostCreatedEvent(Post post) {
        var event = new PostCreatedEvent().post(
                new ApiPostForEvent()
                        .id(post.getId())
                        .ownerId(post.getOwnerId())
                        .ownerEmail(post.getOwnerEmail())
                        .body(post.getBody())
                        .tags(post.getTags())
                        .likeCount(post.getLikeCount())
        );
        this.rabbitTemplate.convertAndSend("x.post-created", "", event);
        LOGGER.info("Sending post-created event: " + event);
    }

    public void sendPostUpdatedEvent(Post post) {
        var event = new PostUpdatedEvent().post(
                new ApiPost()
                        .id(post.getId())
                        .ownerId(post.getOwnerId())
                        .ownerEmail(post.getOwnerEmail())
                        .body(post.getBody())
                        .tags(post.getTags())
                        .likeCount(post.getLikeCount())
                        .likesInUserIds(new ArrayList<>(post.getLikesInUserIds()))
                        .comments(post.getComments().stream().map(this::toCommentDto).toList())
        );
        this.rabbitTemplate.convertAndSend("x.post-updated", "", event);
        LOGGER.info("Sending message: " + event);
    }

    private ApiComment toCommentDto(Comment comment) {
        return new ApiComment()
                .commentId(comment.getId())
                .userId(comment.getUserId())
                .content(comment.getContent());
    }

    public void sendRemovePostEvent(Integer postId, Integer userId) {
        var event = new RemovePostEvent();
        event.setPostId(postId);
        event.setUserId(userId);
        this.rabbitTemplate.convertAndSend("x.post-removed", "", event);
        LOGGER.info("Sending message: " + event);
    }

    public void sendRemoveLikeEvent(Integer postId, Integer userId) {
        var event = new RemoveLikeEvent();
        event.setPostId(postId);
        event.setUserId(userId);
        this.rabbitTemplate.convertAndSend("x.like-removed", "", event);
        LOGGER.info("Sending message: " + event);
    }

    public void sendRemoveCommentEvent(Integer postId, Integer commentId, Integer userId) {
        var event = new RemoveCommentEvent();
        event.setPostId(postId);
        event.setCommentId(commentId);
        event.setUserId(userId);
        this.rabbitTemplate.convertAndSend("x.comment-removed", "", event);
        LOGGER.info("Sending message: " + event);
    }

}
