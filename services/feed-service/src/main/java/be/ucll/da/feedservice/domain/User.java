package be.ucll.da.feedservice.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "`user`")
public class User {

    @Id
    private int id;

    private String name;

    private String email;

    @ElementCollection
    private Set<Integer> friends = new HashSet<>();

    public User() {}

    public User(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Set<Integer> getFriends() {
        return friends;
    }

    public void addFriend(int userId) {
        this.friends.add(userId);
    }
}
