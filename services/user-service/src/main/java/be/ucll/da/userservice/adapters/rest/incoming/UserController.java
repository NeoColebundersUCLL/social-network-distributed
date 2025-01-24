package be.ucll.da.userservice.adapters.rest.incoming;

import be.ucll.da.userservice.api.UserApiDelegate;
import be.ucll.da.userservice.api.model.ApiUser;
import be.ucll.da.userservice.api.model.ApiUsers;
import be.ucll.da.userservice.api.model.FriendRequest;
import be.ucll.da.userservice.domain.User;
import be.ucll.da.userservice.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserController implements UserApiDelegate {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private ApiUser toUserDto(User user) {
        return new ApiUser()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .friends(new ArrayList<>(user.getFriends()));
    }

    @Override
    public ResponseEntity<List<ApiUser>> getUsers() {
        ApiUsers users = new ApiUsers();
        users.addAll(userService.getAllUsers()
                .stream()
                .map(this::toUserDto)
                .toList());
        return ResponseEntity.ok(users);
    }

    @Override
    public ResponseEntity<Void> createUser(ApiUser user) {
        userService.createUser(user);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ApiUser> getUserById(Integer id) {
        User userEntity = userService.getUserById(id);
        ApiUser user = toUserDto(userEntity);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<ApiUser> addFriend(FriendRequest friendRequest) {
        User updatedUser = userService.addFriend(friendRequest.getUserId(), friendRequest.getFriendId());
        ApiUser apiUser = toUserDto(updatedUser);
        return ResponseEntity.ok(apiUser);
    }
}
