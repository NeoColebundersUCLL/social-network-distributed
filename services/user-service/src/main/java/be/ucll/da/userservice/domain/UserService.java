package be.ucll.da.userservice.domain;
import be.ucll.da.userservice.api.model.ApiUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserService {
    private final UserRepository userRepository;
    private final EventSender eventSender;

    @Autowired
    public UserService(UserRepository userRepository, EventSender eventSender) {
        this.userRepository = userRepository;
        this.eventSender = eventSender;
    }

    public void createUser(ApiUser apiUser) {
        User user = new User(
                apiUser.getName(),
                apiUser.getEmail()
        );
        userRepository.save(user);
        eventSender.sendUserCreatedEvent(user);
    };

    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public User getUserById(int id) {
        return userRepository.findById(id);
    }

    public User addFriend(int userId, int friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        if (user == null) throw new UserException("User not found");
        if (friend == null) throw new UserException("Friend not found");

        user.addFriend(friendId);
        friend.addFriend(userId);

        eventSender.sendFriendRequestEvent(user.getId(), friend.getId());
        return userRepository.save(user);
    }

    // user validation
    public List<User> getExistingUsers(List<Integer> tagIds) {
        List<User> userList = new ArrayList<>();
        for (Integer tagId : tagIds) {
            userList.add(getUserById(tagId));
        }
        return userList;
    }

    public User validateUser(int userId) {
        User user = getUserById(userId);
        return user;
    }
}
