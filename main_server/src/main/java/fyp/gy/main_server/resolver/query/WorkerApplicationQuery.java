package fyp.gy.main_server.resolver.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fyp.gy.common.model.WorkerApplication;
import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.repository.WorkerApplicationRepository;
import graphql.GraphQLException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkerApplicationQuery implements GraphQLQueryResolver {

    private final WorkerApplicationRepository repo;

    public WorkerApplicationQuery(WorkerApplicationRepository repo) {
        this.repo = repo;
    }


    public List<WorkerApplication> getWorkerApplications() {
        if (!userDetails().getProfile().getRole().equals("ADMIN")) {
            throw new GraphQLException("Forbidden Access, only admin allowed");
        }

        return repo.findAll();
    }

    private MyUserDetails userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (MyUserDetails) authentication.getPrincipal();
    }

}
