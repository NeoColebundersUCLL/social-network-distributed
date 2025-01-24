package be.ucll.da.postservice.domain.post;

public class PostException extends RuntimeException {
    public PostException(String message) {
        super(message);
    }
}