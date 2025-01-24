package be.ucll.da.feedservice.domain;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public FeedService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public List<Post> getUserFeed(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedException("User with ID " + userId + " not found"));

        List<Post> posts = postRepository.findAllByOwnerOrTagged(userId);

        for (Integer friendId : user.getFriends()) {
            posts.addAll(postRepository.findAllByOwnerId(friendId));
        }

        return posts;
    }

    public Set<Post> searchUserFeed(Integer userId, String query) {
        Set<Post> resultPosts = new HashSet<>();

        for (Post post : getUserFeed(userId)) {
            if (post.getBody().contains(query)) {
                resultPosts.add(post);
            }

            for (Integer tagId : post.getTags()) {
                User user = userRepository.findUserById(tagId);
                if (user.getName().contains(query)) {
                    resultPosts.add(post);
                }
            }

            User ownerOfPostName = userRepository.findUserById(post.getOwnerId());
            if (ownerOfPostName.getName().contains(query)) {
                resultPosts.add(post);
            }
        }
        return resultPosts;
    }
}
