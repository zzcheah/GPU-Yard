package fyp.gy.main_server.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fyp.gy.common.model.Request;
import fyp.gy.main_server.model.User;
import fyp.gy.main_server.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

@Component
@CrossOrigin(origins = "http://localhost:3000")
public class RequestResolver implements GraphQLResolver<Request> {

    private final UserRepository userRepository;

    public RequestResolver(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(Request request) {
        return userRepository.findById(request.getUserID()).orElseThrow(null);
    }

}
