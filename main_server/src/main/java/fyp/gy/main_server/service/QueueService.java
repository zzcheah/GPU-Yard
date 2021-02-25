package fyp.gy.main_server.service;

import fyp.gy.main_server.model.RecordCollection;
import fyp.gy.main_server.model.RequestDetail;
import fyp.gy.main_server.model.Task;
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
                assignJob(task.getTitle(), task.getRequestID());
            } catch (InterruptedException e) {
                logger.error("fail polling request (QueueService)");
                e.printStackTrace();
            }
        }
    });

    public QueueService(
            MongoTemplate template,
            @Value("${gy.machine1}") String machineUrl
    ) {
        this.template = template;
        this.client = WebClient.create(machineUrl);

        // restore queue from mongoDB
        restoreQueue();

        pollingThread.start();
    }

    public void addRequest(Map<String, Object> payload) {

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

    }

    public void freeMachine(String machineID, String title, String requestID, String status) {

        updateRequestStatus(title, requestID, status);

        // delete the completed task from Tasks collection
        if (requestID == null || title == null || requestID.isEmpty() || title.isEmpty())
            clearTask(title, requestID);

        // TODO: notify user

        logger.info("Free-ing a slot from machine #" + machineID);
        machineQueue.add(machineID);

    }

    private void assignJob(String title, String requestID) {

        // can use priority blocking queue for smarting machine allocation
        boolean assigned = false;

        while (!assigned) {

            String assignedMachine = machineQueue.peek();

            try {
                logger.info("Assigning task to machine #" +  assignedMachine);

                // attempt assigning to machine
                String res = client.post()
                        .uri(uriBuilder -> uriBuilder
                                .path("/process/")
                                .queryParam("title", title)
                                .queryParam("requestID", requestID)
                                .build())
                        .retrieve().bodyToMono(String.class).block();

                // if machine took the job (status code = 200)
                logger.info(res);
                machineQueue.take();
                updateRequestStatus(title, requestID, "PROCESSING");
                assigned = true;

            } catch (InterruptedException e) {
                logger.error(String.format("Failed to allocate machine to for Request #%s(%s)", requestID, title));
                e.printStackTrace();
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }

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

    private void restoreQueue() {

        List<Task> jobRestored = template.findAll(Task.class, "Tasks");
//        Collections.sort(jobRestored);

        if (jobRestored.size() == 0) {
            jobQueue.addAll(jobRestored);
            logger.info(String.format("Restored %d Tasks", jobRestored.size()));
        }

    }

    private void clearTask(String title, String requestID) {
        Query q = new Query();
        Criteria c1 = Criteria.where("title").is(title);
        Criteria c2 = Criteria.where("requestID").is(requestID);
        q.addCriteria(new Criteria().andOperator(c1, c2));
        template.remove(q, "Tasks");
//        DeleteResult d = template.remove(q, "Tasks");
//        add logging if needed
    }

}
