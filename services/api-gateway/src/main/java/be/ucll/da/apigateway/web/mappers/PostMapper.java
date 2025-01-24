package be.ucll.da.apigateway.web.mappers;

import be.ucll.da.apigateway.api.model.*;
import be.ucll.da.apigateway.client.feed.model.FeedComment;
import be.ucll.da.apigateway.client.feed.model.FeedPost;
import be.ucll.da.apigateway.client.post.model.ApiComment;
import be.ucll.da.apigateway.client.post.model.ApiPost;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public ApiPost mapToApiPost(SocialNetworkPost socialNetworkPost) {
        return new ApiPost()
                .id(socialNetworkPost.getId())
                .body(socialNetworkPost.getBody())
                .ownerId(socialNetworkPost.getOwnerId())
                .ownerEmail(socialNetworkPost.getOwnerEmail())
                .tags(socialNetworkPost.getTags())
                .likeCount(socialNetworkPost.getLikeCount())
                .likesInUserIds(socialNetworkPost.getLikesInUserIds())
                .comments(socialNetworkPost.getComments().stream()
                        .map(this::mapToApiComment)
                        .toList());
    }

    public ApiComment mapToApiComment(SocialNetworkComment socialNetworkComment) {
        return new ApiComment()
                .commentId(socialNetworkComment.getCommentId())
                .userId(socialNetworkComment.getUserId())
                .content(socialNetworkComment.getContent());
    }

    public SocialNetworkPost mapToSocialNetworkPost(FeedPost feedServicePost) {
        return new SocialNetworkPost()
                .id(feedServicePost.getId())
                .body(feedServicePost.getBody())
                .ownerId(feedServicePost.getOwnerId())
                .ownerEmail(feedServicePost.getOwnerEmail())
                .tags(feedServicePost.getTags())
                .likeCount(feedServicePost.getLikeCount())
                .likesInUserIds(feedServicePost.getLikesInUserIds())
                .comments(feedServicePost.getComments().stream()
                        .map(this::mapToSocialNetworkComment)
                        .toList());
    }

    public SocialNetworkComment mapToSocialNetworkComment(FeedComment feedServiceComment) {
        return new SocialNetworkComment()
                .commentId(feedServiceComment.getCommentId())
                .userId(feedServiceComment.getUserId())
                .content(feedServiceComment.getContent());
    }
}
