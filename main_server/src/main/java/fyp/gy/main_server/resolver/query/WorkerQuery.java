package fyp.gy.main_server.resolver.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fyp.gy.main_server.model.Worker;
import fyp.gy.main_server.repository.WorkerRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkerQuery implements GraphQLQueryResolver {

    private final WorkerRepository repo;

    public WorkerQuery(WorkerRepository repo) {
        this.repo = repo;
    }

    public List<Worker> getWorkerList() {
        System.out.println("HAHAHHAHAH");
        return repo.findAll();
    }

}
