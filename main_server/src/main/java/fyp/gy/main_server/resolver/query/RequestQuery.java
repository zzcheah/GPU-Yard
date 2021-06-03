package fyp.gy.main_server.resolver.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fyp.gy.common.model.Request;
import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.repository.RequestRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestQuery implements GraphQLQueryResolver {

    private final RequestRepository repo;

    public RequestQuery(RequestRepository repo) {
        this.repo = repo;
    }

    public int countRequests() {
        return Math.toIntExact(repo.count());
    }

    public List<Request> getMyRequests() {
        String id = userDetails().getProfile().getId();
        return repo.findByUserID(id);
    }

    public List<Request> getAllRequests() {
        return repo.findAll();
    }

    private MyUserDetails userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (MyUserDetails)authentication.getPrincipal();
    }

}
