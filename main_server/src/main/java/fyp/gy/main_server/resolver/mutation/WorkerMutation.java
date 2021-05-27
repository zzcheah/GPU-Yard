package fyp.gy.main_server.resolver.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fyp.gy.main_server.model.Worker;
import fyp.gy.main_server.repository.WorkerRepository;
import fyp.gy.main_server.resolver.input.RegisterWorkerInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class WorkerMutation implements GraphQLMutationResolver {

    Logger logger = LoggerFactory.getLogger(WorkerMutation.class);

    private final WorkerRepository workerRepo;
    private final MongoTemplate template;

    public WorkerMutation(WorkerRepository workerRepo, MongoTemplate template) {
        this.workerRepo = workerRepo;
        this.template = template;
    }


    public Worker registerWorker(RegisterWorkerInput input)  {

        Worker worker = new Worker();
        worker.setName(input.getName());
        worker.setMaxTasks(input.getMaxTasks());
        worker.setIpAddress(input.getIpAddress());

        logger.info("Successfully registered worker :: "+ input.getName());
        return workerRepo.insert(worker);

    }

}
