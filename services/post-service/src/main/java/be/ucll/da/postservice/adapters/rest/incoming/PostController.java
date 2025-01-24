package be.ucll.da.postservice.adapters.rest.incoming;

import be.ucll.da.postservice.api.PostApiDelegate;
import be.ucll.da.postservice.api.model.*;
import be.ucll.da.postservice.domain.post.Comment;
import be.ucll.da.postservice.domain.post.Post;
import be.ucll.da.postservice.domain.post.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostController implements PostApiDelegate {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // with saga
    @Override
    public ResponseEntity<Void> createPostWithTagsSaga(ApiPost post) {
        postService.createPostRequest(post);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<ApiPost>> getAllPosts() {
        ApiPosts posts = new ApiPosts();
        posts.addAll(postService.getAllPosts()
                .stream()
                .map(this::toPostDto)
                .toList());
        return ResponseEntity.ok(posts);
    }

    @Override
    public ResponseEntity<Void> removePost(Integer postId, Integer userId) {
        postService.removePost(postId, userId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> likePost(LikePostRequest likePostRequest) {
        postService.likePostRequest(likePostRequest.getPostId(), likePostRequest.getUserId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> commentOnPost(Integer postId, ApiComment comment) {
        postService.commentOnPost(postId, comment);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> removeLike(RemoveLikeRequest removeLikeRequest) {
        postService.removeLike(removeLikeRequest.getPostId(), removeLikeRequest.getUserId());
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> removeComment(RemoveCommentRequest removeCommentRequest) {
        postService.removeComment(removeCommentRequest.getPostId(), removeCommentRequest.getCommentId(), removeCommentRequest.getUserId());
        return ResponseEntity.ok().build();
    }

    private ApiPost toPostDto(Post post) {
        return new ApiPost()
                .id(post.getId())
                .body(post.getBody())
                .ownerId(post.getOwnerId())
                .tags(post.getTags())
                .status(post.getStatus().name())
                .isValidOwnerId(post.getIsValidOwnerId())
                .ownerEmail(post.getOwnerEmail())
                .likeCount(post.getLikeCount())
                .likesInUserIds(new ArrayList<>(post.getLikesInUserIds()))
                .comments(post.getComments().stream().map(this::toCommentDto).toList());

    }

    private ApiComment toCommentDto(Comment comment) {
        return new ApiComment()
                .commentId(comment.getId())
                .userId(comment.getUserId())
                .content(comment.getContent());
    }
}
