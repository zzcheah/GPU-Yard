package fyp.gy.main_server.resolver.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fyp.gy.common.model.WorkerApplication;
import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.model.auth.UserProfile;
import fyp.gy.main_server.repository.WorkerApplicationRepository;
import fyp.gy.main_server.resolver.input.RegisterWorkerInput;
import graphql.GraphQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkerApplicationMutation implements GraphQLMutationResolver {

    private final WorkerApplicationRepository repo;
    private final WorkerMutation workerMutation;

    public WorkerApplicationMutation(WorkerApplicationRepository repo, WorkerMutation workerMutation) {
        this.repo = repo;
        this.workerMutation = workerMutation;
    }


    public WorkerApplication approveWorker(String id){

        UserProfile profile = userDetails().getProfile();
        if(!profile.getRole().equals("ADMIN")) throw new GraphQLException("Forbidden Access");

        WorkerApplication wa = repo.findById(id).orElse(null);
        if(wa==null) throw new GraphQLException("WorkerApplication not found!");

        System.out.println("yeah worker approved");
        RegisterWorkerInput input = RegisterWorkerInput.builder()
                .name(wa.getName())
                .maxTasks(wa.getMaxTasks())
                .ipAddress(wa.getIpAddress())
                .build();
        workerMutation.registerWorker(input);
        repo.deleteById(id);

        return wa;

    }


    private MyUserDetails userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (MyUserDetails) authentication.getPrincipal();
    }


}
