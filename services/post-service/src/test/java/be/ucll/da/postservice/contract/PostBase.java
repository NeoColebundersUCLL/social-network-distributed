package be.ucll.da.postservice.contract;

import be.ucll.da.postservice.adapters.rest.incoming.PostController;
import be.ucll.da.postservice.api.PostApiController;

import be.ucll.da.postservice.domain.post.PostService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

// This annotation is not necessary since we are only testing the integration layer
// We don't need to start up the entire application

// @SpringBootTest(classes = PostServiceApplication.class)
public class PostBase {

    @BeforeEach
    public void setup() {
        PostService postService = Mockito.mock(PostService.class);

        RestAssuredMockMvc.standaloneSetup(new PostApiController(new PostController(postService)));

        Mockito.doNothing().when(postService).createPostRequest(Mockito.any());

    }
}
