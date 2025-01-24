package be.ucll.da.apigateway.web.controller;

import be.ucll.da.apigateway.api.SocialNetworkApiDelegate;
import be.ucll.da.apigateway.api.model.*;
import be.ucll.da.apigateway.client.feed.api.FeedApi;
import be.ucll.da.apigateway.client.feed.model.FeedPost;
import be.ucll.da.apigateway.client.post.api.PostApi;
import be.ucll.da.apigateway.client.post.model.*;
import be.ucll.da.apigateway.client.user.api.UserApi;
import be.ucll.da.apigateway.client.user.model.ApiUser;
import be.ucll.da.apigateway.client.user.model.FriendRequest;
import be.ucll.da.apigateway.web.mappers.PostMapper;
import be.ucll.da.apigateway.web.mappers.UserMapper;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SocialNetworkController implements SocialNetworkApiDelegate {

    private final UserApi userApi;
    private final PostApi postApi;
    private final FeedApi feedApi;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final EurekaClient discoveryClient;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    // dependency injection is better than autowired
    @Autowired
    public SocialNetworkController(
            UserApi userApi,
            PostApi postApi,
            FeedApi feedApi,
            CircuitBreakerFactory circuitBreakerFactory,
            EurekaClient discoveryClient,
            PostMapper postMapper,
            UserMapper userMapper
    ) {
        this.userApi = userApi;
        this.postApi = postApi;
        this.feedApi = feedApi;
        this.circuitBreakerFactory = circuitBreakerFactory;
        this.discoveryClient = discoveryClient;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }


    public ResponseEntity<List<SocialNetworkPost>> getUserFeed(Integer userId) {
        String feedServiceUrl = discoveryClient.getNextServerFromEureka("feed-service", false).getHomePageUrl();
        feedApi.getApiClient().setBasePath(feedServiceUrl);

        List<FeedPost> feedServicePosts = circuitBreakerFactory.create("feedApi")
                .run(() -> feedApi.getUserFeed(userId));

        List<SocialNetworkPost> socialNetworkPosts = feedServicePosts.stream()
                .map(postMapper::mapToSocialNetworkPost)
                .toList();

        return ResponseEntity.ok(socialNetworkPosts);
    }


    public ResponseEntity<List<SocialNetworkPost>> searchUserFeed(Integer userId, String query) {
        String feedServiceUrl = discoveryClient.getNextServerFromEureka("feed-service", false).getHomePageUrl();
        feedApi.getApiClient().setBasePath(feedServiceUrl);

        List<FeedPost> feedServicePosts = circuitBreakerFactory.create("feedApi")
                .run(() -> feedApi.searchUserFeed(userId, query));

        List<SocialNetworkPost> socialNetworkPosts = feedServicePosts.stream()
                .map(postMapper::mapToSocialNetworkPost)
                .toList();

        return ResponseEntity.ok(socialNetworkPosts);
    }


    public ResponseEntity<Void> createUser(SocialNetworkUser user) {
        String userServiceUrl = discoveryClient.getNextServerFromEureka("user-service", false).getHomePageUrl();
        userApi.getApiClient().setBasePath(userServiceUrl);

        ApiUser apiUser = new ApiUser()
                .name(user.getName())
                .email(user.getEmail());

        circuitBreakerFactory.create("userApi").run(() -> {
            userApi.createUser(apiUser);
            return null;
        });

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<SocialNetworkUser> addFriend(SocialNetworkFriendRequest request) {
        String userServiceUrl = discoveryClient.getNextServerFromEureka("user-service", false).getHomePageUrl();
        userApi.getApiClient().setBasePath(userServiceUrl);

        FriendRequest friendRequest = new FriendRequest()
                .userId(request.getUserId())
                .friendId(request.getFriendId());

        ApiUser apiUser = circuitBreakerFactory.create("userApi").run(() -> userApi.addFriend(friendRequest));
        SocialNetworkUser responseUser = userMapper.mapToSocialNetworkUser(apiUser);

        return ResponseEntity.ok(responseUser);
    }


    public ResponseEntity<Void> createPost(SocialNetworkPost post) {
        String postServiceUrl = discoveryClient.getNextServerFromEureka("post-service", false).getHomePageUrl();
        postApi.getApiClient().setBasePath(postServiceUrl);

         ApiPost postRequest = postMapper.mapToApiPost(post);

        circuitBreakerFactory.create("postApi").run(() -> {
            postApi.createPostWithTagsSaga(postRequest);
            return null;
        });

        return ResponseEntity.ok().build();
    }


    public ResponseEntity<Void> removePost(Integer postId, Integer userId) {
        String postServiceUrl = discoveryClient.getNextServerFromEureka("post-service", false).getHomePageUrl();
        postApi.getApiClient().setBasePath(postServiceUrl);

        circuitBreakerFactory.create("postApi").run(() -> {
            postApi.removePost(postId, userId);
            return null;
        });

        return ResponseEntity.ok().build();
    }


    public ResponseEntity<Void> likePost(SocialNetworkLikePostRequest likePostRequest) {
        String postServiceUrl = discoveryClient.getNextServerFromEureka("post-service", false).getHomePageUrl();
        postApi.getApiClient().setBasePath(postServiceUrl);

        LikePostRequest request = new LikePostRequest()
                .postId(likePostRequest.getPostId())
                .userId(likePostRequest.getUserId());

        circuitBreakerFactory.create("postApi").run(() -> {
            postApi.likePost(request);
            return null;
        });

        return ResponseEntity.ok().build();
    }


    public ResponseEntity<Void> removeLike(SocialNetworkUnlikePostRequest unlikePostRequest) {
        String postServiceUrl = discoveryClient.getNextServerFromEureka("post-service", false).getHomePageUrl();
        postApi.getApiClient().setBasePath(postServiceUrl);

        RemoveLikeRequest request = new RemoveLikeRequest()
                .postId(unlikePostRequest.getPostId())
                .userId(unlikePostRequest.getUserId());

        circuitBreakerFactory.create("postApi").run(() -> {
            postApi.removeLike(request);
            return null;
        });

        return ResponseEntity.ok().build();
    }


    public ResponseEntity<Void> commentOnPost(Integer postId, SocialNetworkComment comment) {
        String postServiceUrl = discoveryClient.getNextServerFromEureka("post-service", false).getHomePageUrl();
        postApi.getApiClient().setBasePath(postServiceUrl);

        ApiComment apiComment = postMapper.mapToApiComment(comment);

        circuitBreakerFactory.create("postApi").run(() -> {
            postApi.commentOnPost(postId, apiComment);
            return null;
        });

        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Void> removeComment(SocialNetworkRemoveCommentEvent request) {
        String postServiceUrl = discoveryClient.getNextServerFromEureka("post-service", false).getHomePageUrl();
        postApi.getApiClient().setBasePath(postServiceUrl);

        RemoveCommentRequest requestBody = new RemoveCommentRequest()
                .postId(request.getPostId())
                .userId(request.getUserId())
                .commentId(request.getCommentId());

        circuitBreakerFactory.create("postApi").run(() -> {
            postApi.removeComment(requestBody);
            return null;
        });

        return ResponseEntity.ok().build();
    }
}
