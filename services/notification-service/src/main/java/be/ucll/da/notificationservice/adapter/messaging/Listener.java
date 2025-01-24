package be.ucll.da.notificationservice.adapter.messaging;

import be.ucll.da.notificationservice.client.post.api.model.SendEmailEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class Listener {
    private final static Logger LOGGER = LoggerFactory.getLogger(Listener.class);

    @RabbitListener(queues = {"q.post-notification-service"})
    public void onEmailEvent(SendEmailEvent event) {
        LOGGER.info("Sending email with text: " + event.getMessage() +
                " :to recipient " + event.getRecipient() + ".");
    }
}
