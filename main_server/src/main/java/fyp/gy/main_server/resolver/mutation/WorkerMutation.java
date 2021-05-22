package fyp.gy.main_server.resolver.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fyp.gy.main_server.model.Worker;
import fyp.gy.main_server.repository.WorkerRepository;
import fyp.gy.main_server.resolver.input.RegisterWorkerInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

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

    private Map<String, Object> decodeJson(String encodedPayload) throws JsonProcessingException {

        byte[] decodedBytes = Base64.getDecoder().decode(encodedPayload);
        String decodedString = new String(decodedBytes);

        return new ObjectMapper().readValue(decodedString, new TypeReference<Map<String, Object>>() {
        });
    }
}
