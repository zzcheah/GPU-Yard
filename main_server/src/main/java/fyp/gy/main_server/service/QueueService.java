package fyp.gy.main_server.service;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import fyp.gy.common.constant.GyConstant;
import fyp.gy.common.model.Request;
import fyp.gy.main_server.model.Task;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.PriorityBlockingQueue;

@Service
public class QueueService {

    Logger logger = LoggerFactory.getLogger(QueueService.class);

    private final WebClient client;
    private final MongoTemplate template;

    PriorityBlockingQueue<Task> jobQueue = new PriorityBlockingQueue<>();
    BlockingQueue<String> machineQueue = new LinkedBlockingDeque<>();

    Thread pollingThread = new Thread(() -> {
        while (true) {
            logger.info("Checking for job in queue...");
            try {
                Task task = jobQueue.take();
                try {
                    assignJob(task.getRequestID());
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

    public QueueService(MongoTemplate template) {
        this.template = template;
        this.client = WebClient.create("http://localhost:8181");

        // restore queue from mongoDB
        restoreQueue();

        pollingThread.start();
    }

    public String addRequest(Map<String, Object> payload) {

        String title = (String) payload.get("title");

        Request detail = new Request();
        detail.setStatus("NEW");
        detail.setUserID((String) payload.get("userID"));
        detail.setCreatedAt((String) payload.get("createdAt"));
        detail.setTitle(title);
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

    public void freeMachine(String machineID, String requestID, String status) {


        // delete the completed task from Tasks collection
        if (requestID != null && !requestID.isEmpty()) {
            updateRequestStatus(requestID, status);
            clearTask(requestID);
        }

        // TODO: notify user

        logger.info("Free-ing a slot from machine #" + machineID);
        machineQueue.add(machineID);

    }

    private void assignJob(String requestID) throws Exception {

        // can use priority blocking queue for smart machine allocation
        boolean assigned = false;

        while (!assigned) {

            try {
                String assignedMachine = machineQueue.take();
                logger.info(String.format("Assigning Task #%s to Machine #%s", requestID, assignedMachine));


                // attempt assigning to machine
                String res = client.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/process/")
                                .queryParam("requestID", requestID)
                                .build())
                        .retrieve().bodyToMono(String.class).block();

                // if machine took the job (status code = 200)
                logger.info(res);
                updateRequestStatus(requestID, "PROCESSING");
                assigned = true;

            } catch (Exception e) {
                updateRequestStatus(requestID, "FAILED ALLOCATING MACHINE");
                logger.error(e.getMessage());
                throw e;
//                e.printStackTrace();
            }

        }

    }

    private void updateRequestStatus(String requestID, String status) {

        Query q = new Query(Criteria.where("_id").is(new ObjectId(requestID)));
        Update u = new Update().set("status", status);
        UpdateResult result = template.updateFirst(q, u, GyConstant.REQUESTS_COLLECTION);
        System.out.println(result.getModifiedCount());

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
