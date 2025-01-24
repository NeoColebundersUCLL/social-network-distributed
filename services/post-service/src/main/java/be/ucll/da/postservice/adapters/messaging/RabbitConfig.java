package be.ucll.da.postservice.adapters.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
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
        ObjectMapper mapper =
                new ObjectMapper()
                        .registerModule(new ParameterNamesModule())
                        .registerModule(new Jdk8Module())
                        .registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setDateFormat(new StdDateFormat());

        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(Jackson2JsonMessageConverter converter, CachingConnectionFactory cachingConnectionFactory) {
        var template = new RabbitTemplate(cachingConnectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    // send notification
    @Bean
    public Declarables sendEmailEventSchema(){
        return new Declarables(
                new FanoutExchange("x.user-tagged"),
                new Queue("q.post-notification-service" ),
                new Binding(
                        "q.post-notification-service",
                        Binding.DestinationType.QUEUE,
                        "x.user-tagged",
                        "post-notification-service",
                        null)
        );
    }

    // validating tagged users
    @Bean
    public Declarables createValidateTaggedUsersQueue(){
        return new Declarables(new Queue("q.user-service.validate-tagged-users"));
    }

    @Bean
    public Declarables createTaggedUsersValidatedExchange(){
        return new Declarables(
                new FanoutExchange("x.tagged-users-validated"),
                new Queue("q.tagged-user-validated.post-service" ),
                new Binding("q.tagged-user-validated.post-service", Binding.DestinationType.QUEUE, "x.tagged-users-validated", "q.tagged-user-validated.post-service", null));
    }

    // validating owner
    @Bean
    public Declarables createValidateOwnerQueue(){
        return new Declarables(new Queue("q.user-service.validate-owner"));
    }

    @Bean
    public Declarables createPostValidatedExchange(){
        return new Declarables(
                new FanoutExchange("x.owner-validated"),
                new Queue("q.owner-validated.post-service" ),
                new Binding("q.owner-validated.post-service", Binding.DestinationType.QUEUE, "x.owner-validated", "q.owner-validated.post-service", null));
    }

    // validating user for post like
    @Bean
    public Declarables createValidateUserForPostLIkeQueue(){
        return new Declarables(new Queue("q.user-service.validate-post-like-user"));
    }

    @Bean
    public Declarables PostLikeUserValidatedEventExchange(){
        return new Declarables(
                new FanoutExchange("x.user-like-validated"),
                new Queue("q.user-like-validated.post-service" ),
                new Binding("q.user-like-validated.post-service", Binding.DestinationType.QUEUE, "x.user-like-validated", "q.user-like-validated.post-service", null));
    }

    // validating user for post comment
    @Bean
    public Declarables createValidateUserForPostCommentQueue(){
        return new Declarables(new Queue("q.user-service.validate-post-comment-user"));
    }

    @Bean
    public Declarables PostCommentUserValidatedEventExchange(){
        return new Declarables(
                new FanoutExchange("x.user-comment-validated"),
                new Queue("q.user-comment-validated.post-service" ),
                new Binding("q.user-comment-validated.post-service", Binding.DestinationType.QUEUE, "x.user-comment-validated", "q.user-like-validated.post-service", null));
    }

    // post created event
    @Bean
    public Declarables CreatePostEventSchema(){
        return new Declarables(
                new FanoutExchange("x.post-created"),
                new Queue("q.post-feed-service" ),
                new Binding(
                        "q.post-feed-service",
                        Binding.DestinationType.QUEUE,
                        "x.post-created",
                        "post-feed-service",
                        null)
        );
    }

    // post updated event
    @Bean
    public Declarables UpdatePostEventSchema(){
        return new Declarables(
                new FanoutExchange("x.post-updated"),
                new Queue("q.post-feed-service-update" ),
                new Binding(
                        "q.post-feed-service-update",
                        Binding.DestinationType.QUEUE,
                        "x.post-updated",
                        "post-feed-service-update",
                        null)
        );
    }

    // post removed event
    @Bean
    public Declarables RemovePostEventSchema(){
        return new Declarables(
                new FanoutExchange("x.post-removed"),
                new Queue("q.post-removed-feed-service" ),
                new Binding(
                        "q.post-removed-feed-service",
                        Binding.DestinationType.QUEUE,
                        "x.post-removed",
                        "q.post-removed-feed-service",
                        null)
        );
    }

    // like removed event
    @Bean
    public Declarables RemoveLikeEventSchema(){
        return new Declarables(
                new FanoutExchange("x.like-removed"),
                new Queue("q.like-removed-feed-service" ),
                new Binding(
                        "q.like-removed-feed-service",
                        Binding.DestinationType.QUEUE,
                        "x.like-removed",
                        "q.like-removed-feed-service",
                        null)
        );
    }

    // comment removed event
    @Bean
    public Declarables RemoveCommentEventSchema(){
        return new Declarables(
                new FanoutExchange("x.comment-removed"),
                new Queue("q.comment-removed-feed-service" ),
                new Binding(
                        "q.comment-removed-feed-service",
                        Binding.DestinationType.QUEUE,
                        "x.comment-removed",
                        "q.comment-removed-feed-service",
                        null)
        );
    }
}
