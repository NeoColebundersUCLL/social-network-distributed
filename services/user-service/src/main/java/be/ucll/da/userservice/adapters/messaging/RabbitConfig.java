package be.ucll.da.userservice.adapters.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(Jackson2JsonMessageConverter converter, CachingConnectionFactory cachingConnectionFactory) {
        var template = new RabbitTemplate(cachingConnectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public Declarables createUserCreatedSchema(){
        return new Declarables(
                new FanoutExchange("x.user-created"),
                new Queue("q.user-feed-service" ),
                new Binding(
                        "q.user-feed-service",
                        Binding.DestinationType.QUEUE,
                        "x.user-created",
                        "user-post-service",
                        null)
        );
    }

    @Bean
    public Declarables createFriendRequestEventSchema(){
        return new Declarables(
                new FanoutExchange("x.friend-added"),
                new Queue("q.friend-service" ),
                new Binding(
                        "q.friend-service",
                        Binding.DestinationType.QUEUE,
                        "x.friend-added",
                        "friend-service",
                        null)
        );
    }
}
