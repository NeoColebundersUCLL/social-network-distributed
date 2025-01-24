package be.ucll.da.feedservice.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends CrudRepository<Post, Integer> {

    @Query("SELECT p FROM Post p WHERE p.ownerId = :userId OR :userId IN elements(p.tags)")
    List<Post> findAllByOwnerOrTagged(Integer userId);

    List<Post> findAllByOwnerId(Integer userId);
    Post findPostById(Integer postId);
}
