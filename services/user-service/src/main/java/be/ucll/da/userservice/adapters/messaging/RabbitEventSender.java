package be.ucll.da.userservice.adapters.messaging;

import be.ucll.da.userservice.api.model.ApiUser;
import be.ucll.da.userservice.api.model.FriendRequest;
import be.ucll.da.userservice.api.model.UserCreatedEvent;
import be.ucll.da.userservice.domain.EventSender;
import be.ucll.da.userservice.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RabbitEventSender implements EventSender {
    private final static Logger LOGGER = LoggerFactory.getLogger(RabbitEventSender.class);
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitEventSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendUserCreatedEvent(User user) {
        this.rabbitTemplate.convertAndSend("x.user-created", "", toEvent(user));
        LOGGER.info("User created event send: {}", toEvent(user));
    }

    @Override
    public void sendFriendRequestEvent(Integer userId, Integer friendId) {
        var event = new FriendRequest();
        event.setUserId(userId);
        event.setFriendId(friendId);
        this.rabbitTemplate.convertAndSend("x.friend-added", "", event);
        LOGGER.info("Friend request event sent to user {}", userId);
    }

    private UserCreatedEvent toEvent(User user) {
        return new UserCreatedEvent()
                .user(new ApiUser()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail()));
    }
}
