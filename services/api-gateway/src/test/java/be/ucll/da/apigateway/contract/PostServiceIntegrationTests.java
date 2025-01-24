package be.ucll.da.apigateway.contract;

import be.ucll.da.apigateway.client.post.ApiClient;
import be.ucll.da.apigateway.client.post.api.PostApi;
import be.ucll.da.apigateway.client.post.model.ApiPost;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureStubRunner(
        ids = {"be.ucll.da:post-service:0.0.1-SNAPSHOT:stubs:6565"},
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
public class PostServiceIntegrationTests {

    @Test
    void create_post() {
        ApiClient apiClient = new ApiClient(new RestTemplate());
        apiClient.setBasePath("http://localhost:6565");

        PostApi postApi = new PostApi(apiClient);

        // Prepare the request payload
        ApiPost postRequest = new ApiPost();
        postRequest.setOwnerId(1);
        postRequest.setBody("This is a sample post");
        postRequest.setTags(List.of(1, 2, 3));

        Assertions.assertThatCode(() -> postApi.createPostWithTagsSaga(postRequest))
                .doesNotThrowAnyException();
    }
}

