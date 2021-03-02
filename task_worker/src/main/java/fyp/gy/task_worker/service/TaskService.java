package fyp.gy.task_worker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final MongoTemplate template;

    public TaskService(MongoTemplate template) {
        this.template = template;
    }

    public void process(String title, String requestID) {

    }


}
