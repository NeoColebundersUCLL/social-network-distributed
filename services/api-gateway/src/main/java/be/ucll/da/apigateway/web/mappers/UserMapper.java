package be.ucll.da.apigateway.web.mappers;

import be.ucll.da.apigateway.api.model.SocialNetworkUser;
import be.ucll.da.apigateway.client.user.model.ApiUser;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public SocialNetworkUser mapToSocialNetworkUser(ApiUser user) {
        return new SocialNetworkUser()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .friends(user.getFriends());
    }
}
