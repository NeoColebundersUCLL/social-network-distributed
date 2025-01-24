package be.ucll.da.postservice.domain.post;


import org.springframework.data.repository.CrudRepository;

public interface PostRepository extends CrudRepository<Post, Long> {
    Post findById(int id);
}