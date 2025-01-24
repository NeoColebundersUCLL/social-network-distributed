package be.ucll.da.feedservice.adapters.messaging;

import be.ucll.da.feedservice.client.post.api.model.*;
import be.ucll.da.feedservice.client.user.model.ApiUser;
import be.ucll.da.feedservice.client.user.model.FriendRequest;
import be.ucll.da.feedservice.client.user.model.UserCreatedEvent;
import be.ucll.da.feedservice.domain.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class Listener {

    private final static Logger LOGGER = LoggerFactory.getLogger(Listener.class);
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public Listener(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    // user-service events
    @RabbitListener(queues = {"q.user-feed-service"})
    public void onUserCreatedEvent(UserCreatedEvent event) {
        LOGGER.info("Received user created event: {}", event);

        ApiUser eventUser = event.getUser();
        User user = new User(eventUser.getId(), eventUser.getName(), eventUser.getEmail());
        userRepository.save(user);
    }

    @RabbitListener(queues = {"q.friend-service"})
    public void onFriendRequestEvent(FriendRequest event) {
        LOGGER.info("Received friend request event: {}", event);

        User user = userRepository.findUserById(event.getUserId());
        User friend = userRepository.findUserById(event.getFriendId());

        user.addFriend(event.getFriendId());
        friend.addFriend(event.getUserId());

        userRepository.save(user);
        userRepository.save(friend);
    }

    // post-service events
    @RabbitListener(queues = {"q.post-feed-service"})
    public void onPostCreatedEvent(PostCreatedEvent event) {
        LOGGER.info("Received post created event: {}", event);

        ApiPostForEvent eventPost = event.getPost();
        Post post = new Post(
                eventPost.getId(),
                eventPost.getOwnerId(),
                eventPost.getOwnerEmail(),
                eventPost.getBody(),
                eventPost.getTags(),
                eventPost.getLikeCount()
        );
        postRepository.save(post);
    }

    @RabbitListener(queues = {"q.post-feed-service-update"})
    public void onPostUpdatedEvent(PostUpdatedEvent event) {
        LOGGER.info("Received post updated event: {}", event);

        ApiPost eventPost = event.getPost();
        Post post = postRepository.findPostById(eventPost.getId());

        // Update basic fields
        post.setBody(eventPost.getBody());
        post.setTags(eventPost.getTags());
        post.setLikeCount(eventPost.getLikeCount());

        // Update likes
        post.getLikesInUserIds().clear();
        post.getLikesInUserIds().addAll(eventPost.getLikesInUserIds());

        // Replace comments
        post.getComments().clear(); // Clear existing comments
        for (ApiComment apiComment : eventPost.getComments()) {
            post.addComment(new Comment(apiComment.getCommentId(), apiComment.getUserId(), apiComment.getContent()));
        }

        postRepository.save(post);
    }

    @RabbitListener(queues = {"q.post-removed-feed-service"})
    public void onRemovedPostEvent(RemovePostEvent event) {
        LOGGER.info("Received removed post event: {}", event);

        Post post = postRepository.findPostById(event.getPostId());
        postRepository.delete(post);
    }

    @RabbitListener(queues = {"q.like-removed-feed-service"})
    public void onRemoveLikeEvent(RemoveLikeEvent event) {
        LOGGER.info("Received removed like event: {}", event);

        Post post = postRepository.findPostById(event.getPostId());
        post.removeLike(event.getUserId());
        postRepository.save(post);
    }

    @RabbitListener(queues = {"q.comment-removed-feed-service"})
    public void onRemoveCommentEvent(RemoveCommentEvent event) {
        LOGGER.info("Received removed comment event: {}", event);

        Post post = postRepository.findPostById(event.getPostId());
        post.getComments().removeIf(comment -> comment.getId() == event.getCommentId());
        postRepository.save(post);

        LOGGER.info("Comment with id: {} removed from post id: {}", event.getCommentId(), event.getPostId());
    }

}
