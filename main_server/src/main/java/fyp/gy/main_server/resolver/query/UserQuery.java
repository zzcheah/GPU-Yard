package fyp.gy.main_server.resolver.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fyp.gy.main_server.model.User;
import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserQuery implements GraphQLQueryResolver {

    private final UserRepository repo;

    public UserQuery(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    private MyUserDetails userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (MyUserDetails)authentication.getPrincipal();
    }

}
