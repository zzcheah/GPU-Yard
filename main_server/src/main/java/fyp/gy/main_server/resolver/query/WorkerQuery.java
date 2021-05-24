package fyp.gy.main_server.resolver.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fyp.gy.main_server.model.Worker;
import fyp.gy.main_server.service.WorkerService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkerQuery implements GraphQLQueryResolver {

    private final WorkerService workerService;

    public WorkerQuery(WorkerService workerService) {
        this.workerService = workerService;
    }

    public List<Worker> getWorkerList() {
        return workerService.getWorkerList();
    }

}
