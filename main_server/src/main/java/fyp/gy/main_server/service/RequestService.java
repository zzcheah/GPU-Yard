package fyp.gy.main_server.service;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import fyp.gy.common.constant.GyConstant;
import fyp.gy.common.model.Request;
import fyp.gy.main_server.model.Task;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class RequestService {

    Logger logger = LoggerFactory.getLogger(RequestService.class);

    // Autowired
    private final MongoTemplate template;
    private final MachineService machineService;

    // use internally
    private final PriorityBlockingQueue<Task> jobQueue = new PriorityBlockingQueue<>();


    Thread pollingThread = new Thread(() -> {
        while (true) {
            logger.info("Checking for job in queue...");
            try {
                Task task = jobQueue.take();
                try {
                    logger.info(String.format("Request #%s polled, looking for machine available...", task.getRequestID()));
                    processRequest(task.getRequestID());
                } catch (Exception e) {
                    jobQueue.add(task);
                    e.printStackTrace();
                }
            } catch (InterruptedException e) {
                logger.error("fail polling request (QueueService): " + e.getMessage());
                e.printStackTrace();
            }
        }
    });

    public RequestService(MongoTemplate template, MachineService machineService) {
        this.template = template;
        this.machineService = machineService;

        // restore queue from mongoDB
        restoreQueue();
        pollingThread.start();
    }

    public String addRequest(Map<String, Object> payload) {

        Request detail = new Request();
        detail.setStatus("NEW");
        detail.setUserID((String) payload.get("userID"));
        detail.setCreatedAt((String) payload.get("createdAt"));
        detail.setTitle((String) payload.get("title"));
        detail.setImage((String) payload.get("image"));
        detail.setEncodedParam((String) payload.get("param"));
        //noinspection unchecked
        detail.setInputFiles((List<String>) payload.get("inputFiles"));

        detail = template.insert(detail, GyConstant.REQUESTS_COLLECTION);

        Task task = new Task();
        task.setRequestID(detail.getId());
        task.setCreatedAt(detail.getCreatedAt());

        // Record in mongo for persistency
        template.insert(task, GyConstant.TASKS_COLLECTION);

        // Volatile job queue
        jobQueue.add(task);
        logger.info("Added request #" + detail.getId() + " to job queue");
        return detail.getId();
    }

    public void completeRequest(String requestID, String machineID, String remark) {

        if (requestID != null && !requestID.isEmpty()) {

            Query q = new Query(Criteria.where("_id").is(new ObjectId(requestID)));
            Update u = new Update();
            u.set("status", "COMPLETED");
            u.set("remark", remark);

            UpdateResult r = template.updateFirst(q, u, GyConstant.REQUESTS_COLLECTION);
            logger.info(String.format("Updating Request #%s", requestID));

            clearTask(requestID);
            logger.info(String.format("Request #%s completed.", requestID));

            // notify user;
        }

        machineService.releaseMachine(machineID);


    }


    // util func

    private void processRequest(String requestID) {
        try {

            machineService.allocateMachine(requestID);
            updateRequestStatus(requestID, "PROCESSING");
        } catch (Exception e) {
            updateRequestStatus(requestID, "FAILED_ALLOCATING_MACHINE");
            e.printStackTrace();
        }
    }

    private void updateRequestStatus(String requestID, String status) {

        Query q = new Query(Criteria.where("_id").is(new ObjectId(requestID)));
        Update u = new Update().set("status", status);
        UpdateResult result = template.updateFirst(q, u, GyConstant.REQUESTS_COLLECTION);
        logger.info(String.format("Updated Request #%s Status to %s", requestID, status));

    }

    private void restoreQueue() {

        List<Task> jobRestored = template.findAll(Task.class, GyConstant.TASKS_COLLECTION);
//        Collections.sort(jobRestored);

        if (jobRestored.size() != 0) {
            jobQueue.addAll(jobRestored);
            logger.info(String.format("Restored %d Tasks", jobRestored.size()));
        }

    }

    private void clearTask(String requestID) {

        Query q = new Query(Criteria.where("requestID").is(requestID));
        DeleteResult d = template.remove(q, GyConstant.TASKS_COLLECTION);
        if (d.getDeletedCount() != 0)
            logger.warn(String.format("Task #%s was not removed from Tasks Collection", requestID));
        else
            logger.info(String.format("Task #%s was removed from Tasks Collection", requestID));

    }
}
