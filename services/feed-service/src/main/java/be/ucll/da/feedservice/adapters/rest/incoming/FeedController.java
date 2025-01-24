package be.ucll.da.feedservice.adapters.rest.incoming;

import be.ucll.da.feedservice.api.FeedApiDelegate;
import be.ucll.da.feedservice.api.model.FeedComment;
import be.ucll.da.feedservice.api.model.FeedPost;
import be.ucll.da.feedservice.api.model.FeedPosts;
import be.ucll.da.feedservice.domain.Comment;
import be.ucll.da.feedservice.domain.FeedService;
import be.ucll.da.feedservice.domain.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FeedController implements FeedApiDelegate {
    private final FeedService service;

    public FeedController(FeedService service) {
        this.service = service;
    }

    @Override
    public ResponseEntity<List<FeedPost>> getUserFeed(Integer userId) {
        FeedPosts posts = new FeedPosts();
        posts.addAll(service.getUserFeed(userId)
                .stream()
                .map(this::toFeedPostDto)
                .toList());
        return ResponseEntity.ok(posts);
    }

    @Override
    public ResponseEntity<List<FeedPost>> searchUserFeed(Integer userId, String query) {
        Set<FeedPost> postsSet = service.searchUserFeed(userId, query)
                .stream()
                .map(this::toFeedPostDto)
                .collect(Collectors.toSet());

        List<FeedPost> postList = new ArrayList<>(postsSet);
        return ResponseEntity.ok(postList);
    }

    private FeedPost toFeedPostDto(Post post) {
        return new FeedPost()
                .id(post.getId())
                .body(post.getBody())
                .ownerId(post.getOwnerId())
                .tags(post.getTags())
                .ownerEmail(post.getOwnerEmail())
                .likeCount(post.getLikeCount())
                .likesInUserIds(new ArrayList<>(post.getLikesInUserIds()))
                .comments(post.getComments().stream().map(this::toCommentDto).toList());
    }

    private FeedComment toCommentDto(Comment comment) {
        return new FeedComment()
                .commentId(comment.getId())
                .userId(comment.getUserId())
                .content(comment.getContent());
    }
}

