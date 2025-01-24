package be.ucll.da.postservice.repository;

import be.ucll.da.postservice.domain.post.Post;
import be.ucll.da.postservice.domain.post.PostRepository;
import be.ucll.da.postservice.domain.post.PostStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@DataJpaTest // word gebruikt om Jpa respos te testen
@ExtendWith(SpringExtension.class)
//  Integrates the JUnit 5 testing framework (JUnit Jupiter) with the Spring TestContext Framework.
public class FindPostTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PostRepository postRepository;

    @Test
    void findPostById() {
        var post = new Post(1, "this is a post", List.of(1, 2));

        entityManager.persist(post);

        var jpaPost = postRepository.findById(post.getId());
        Assertions.assertNotNull(jpaPost);
    }
}
