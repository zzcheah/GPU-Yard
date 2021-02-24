package fyp.gy.main_server.service;

import fyp.gy.main_server.model.RecordCollection;
import fyp.gy.main_server.model.RequestDetail;
import fyp.gy.main_server.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class QueueService {

    Logger logger = LoggerFactory.getLogger(QueueService.class);

    private final MongoTemplate template;

    BlockingQueue<Task> jobQueue = new LinkedBlockingDeque<>();
    BlockingQueue<String> machineQueue = new LinkedBlockingDeque<>();

    Thread pollingThread = new Thread(() -> {
        while (true) {
            logger.info("Polling...");
            try {
                Task task = jobQueue.take();
                assignJob(task.getTitle(), task.getRequestID());
            } catch (InterruptedException e) {
                logger.error("fail polling request (QueueService)");
            }
        }
    });

    public QueueService(MongoTemplate template) {
        this.template = template;
        pollingThread.start();
        // TODO: restore queue from mongoDB
    }

    public String addRequest(Map<String, Object> payload) {

        String title = (String) payload.get("title");
        String requestID = (String) payload.get("requestID");

        RequestDetail detail = new RequestDetail();
        detail.setStatus("NEW");
        detail.setRequestID(requestID);
        detail.setUserID((String) payload.get("userID"));
        detail.setCreatedAt((String) payload.get("createdAt"));
        detail.setTitle(title);
        detail.setEncodedParam((String) payload.get("param"));
        //noinspection unchecked
        detail.setInputFiles((List<String>) payload.get("inputFiles"));

        Query query = new Query();
        query.addCriteria(Criteria.where("title").is(title));

        if (!template.exists(query, "RecordCollection")) {
            logger.info("Creating new RecordCollection: " + title + "...");
            RecordCollection recordCollection = new RecordCollection();
            recordCollection.setTitle(title);
            template.insert(recordCollection, "RecordCollection");
        }

        Update update = new Update();

        update.push("requests", detail);
        template.updateFirst(query, update, "RecordCollection");

        Task task = new Task();
        task.setTitle(title);
        task.setRequestID(requestID);
        task.setCreatedAt(detail.getCreatedAt());

        // Record in mongo for persistency
        template.insert(task, "Tasks");

        // Volatile job queue
        jobQueue.add(task);
        logger.info("Added request #" + detail.getRequestID() + " to job queue");

        return "Successfully added request #" + detail.getRequestID() + " to job queue";

    }

    public void freeMachine(String machineID, String title, String requestID, String status) {

        updateRequestStatus(title,requestID,status);
        // TODO: add remark or error message (consider changing the param to json)

        // delete task from Tasks collection
        Query q = new Query(Criteria.where("requestID").is(requestID));
        template.remove(q,"Tasks");

        // TODO: notify user

        logger.info("Free-ing a slot from machine #" + machineID);
        machineQueue.add(machineID);

    }

    private void assignJob(String title, String requestID) {
        try {
            String assignedMachine = machineQueue.take();
            updateRequestStatus(title, requestID, "PROCESSING");
            logger.info("Assigning request #" + requestID + " to machine #" + assignedMachine);
            // TODO: send API call to ask machine to process the job
        } catch (InterruptedException e) {
            logger.error("fail allocating machine for request #" + requestID);
        }

    }

    private void updateRequestStatus(String title, String requestID, String status) {

        Query q = new Query();
        Criteria c1 = Criteria.where("title").is(title);
        Criteria c2 = Criteria.where("requests.requestID").is(requestID);
        q.addCriteria(new Criteria().andOperator(c1, c2));
        Update u = new Update().set("requests.$.status", status);
        template.updateFirst(q, u, "RecordCollection");

    }

}
