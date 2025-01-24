package be.ucll.da.postservice.unit;

import be.ucll.da.postservice.adapters.messaging.RabbitEventSender;
import be.ucll.da.postservice.client.user.model.OwnerValidatedEvent;
import be.ucll.da.postservice.client.user.model.TaggedUsersValidatedEvent;
import be.ucll.da.postservice.domain.post.Post;
import be.ucll.da.postservice.domain.post.PostRepository;
import be.ucll.da.postservice.domain.post.sagas.CreatePostSaga;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import static org.mockito.Mockito.*;

public class CreatePostSagaTests {

    private RabbitEventSender eventSender;
    private PostRepository postRepository;
    private CreatePostSaga createPostSaga;

    @BeforeEach
    void setup() {
        eventSender = Mockito.mock(RabbitEventSender.class);
        postRepository = Mockito.mock(PostRepository.class);
        createPostSaga = new CreatePostSaga(eventSender, postRepository);
    }

    @Test
    void whenValidatingOwner_sendValidateOwnerCommand() {
        // Arrange
        Post post = new Post(1, "Test Post Body", List.of(1, 2));

        // Act
        createPostSaga.executeSaga(post);

        // Assert
        verify(eventSender, times(1)).sendValidateOwnerCommand(post.getId(), post.getOwnerId());
    }

    @Test
    void whenOwnerIsValid_sendValidateTaggedUsersCommand() {
        // Arrange
        OwnerValidatedEvent event = new OwnerValidatedEvent();
        event.setIsOwner(true);
        event.setEmail("owner@test.com");

        Post post = new Post(1, "Test Post Body", List.of(1, 2));
        when(postRepository.findById(1)).thenReturn(post);

        // Act
        createPostSaga.executeSaga(1, event);

        // Assert
        verify(eventSender, times(1)).sendValidateTaggedUsersCommand(1, post.getTags());
        verify(postRepository, never()).delete(any());
    }

    @Test
    void whenOwnerIsInvalid_rollbackPostCreation() {
        // Arrange
        OwnerValidatedEvent event = new OwnerValidatedEvent();
        event.setIsOwner(false);

        Post post = new Post(1, "Test Post Body", List.of(1, 2));
        when(postRepository.findById(1)).thenReturn(post);

        // Act
        createPostSaga.executeSaga(1, event);

        // Assert
        verify(postRepository, times(1)).delete(post);
        verify(eventSender, never()).sendValidateTaggedUsersCommand(any(), any());
    }

    @Test
    void whenTaggedUsersAreValid_sendPostCreatedEvent() {
        // Arrange
        TaggedUsersValidatedEvent event = new TaggedUsersValidatedEvent();
        event.setTaggedUsersValid(true);
        event.setTags(List.of());

        Post post = new Post(1, "Test Post Body", List.of(1, 2));
        when(postRepository.findById(1)).thenReturn(post);

        // Act
        createPostSaga.executeSaga(1, event);

        // Assert
        verify(eventSender, times(1)).sendPostCreatedEvent(post);
    }

    @Test
    void whenTaggedUsersAreInvalid_rollbackPostCreation() {
        // Arrange
        TaggedUsersValidatedEvent event = new TaggedUsersValidatedEvent();
        event.setTaggedUsersValid(false);

        Post post = new Post(1, "Test Post Body", List.of(1, 2));
        when(postRepository.findById(1)).thenReturn(post);

        // Act
        createPostSaga.executeSaga(1, event);

        // Assert
        verify(postRepository, times(1)).delete(post);
        verify(eventSender, never()).sendPostCreatedEvent(post);
    }
}
