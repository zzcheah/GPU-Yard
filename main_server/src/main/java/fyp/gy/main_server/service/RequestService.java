package fyp.gy.main_server.service;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import fyp.gy.common.constant.GyConstant;
import fyp.gy.common.model.Notification;
import fyp.gy.common.model.Request;
import fyp.gy.main_server.model.Task;
import fyp.gy.main_server.repository.RequestRepository;
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
import java.util.PriorityQueue;

@Service
public class RequestService {

    Logger logger = LoggerFactory.getLogger(RequestService.class);

    private final MongoTemplate template;
    private final WorkerService workerService;
    private final RequestRepository requestRepo;
    public final PriorityQueue<Task> jobQueue = new PriorityQueue<>();

    public RequestService(MongoTemplate template, WorkerService workerService, RequestRepository requestRepo) {
        this.template = template;
        this.workerService = workerService;
        this.requestRepo = requestRepo;
        restoreQueue();
    }

    // Correspond to Controller
    public Request addRequest(Map<String, Object> payload) {

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
        return detail;
    }

    public void completeRequest(String requestID, String status, String remark, String fileID) {

        try {

            Request request = template.findById(new ObjectId(requestID),Request.class);
            if(request==null) throw new NullPointerException("Missing Request # "+ requestID);

            request.setStatus(status);
            request.setRemark(remark);
            if(fileID!=null) {
                request.getOutputFiles().add(fileID);
            }

            requestRepo.save(request);

            logger.info(String.format("Request #%s updated, Status: %s.", requestID, status));

            // notify user
            Notification notification = Notification.builder()
                    .content(String.format("Your request # %s has completed.",requestID))
                    .isRead(false)
                    .severity("info")
                    .user(request.getUserID())
                    .build();

            template.save(notification);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Request getJob(String workerName) {

        try {

            if (!workerService.allowPolling(workerName)) return null;

            logger.info(String.format("Worker #%s pooling job queue", workerName));

            Task task = jobQueue.poll();
            if (task == null) return null;

            String requestID = task.getRequestID();
            Request request = template.findById(new ObjectId(requestID), Request.class, GyConstant.REQUESTS_COLLECTION);

            if (request == null) {
                logger.warn(String.format("Cannot locate request #%s in db", requestID));
                return null;
            }

            workerService.addToWorkerTasks(workerName, requestID);

            logger.info(String.format("Request #%s is assigned to Worker #%s", requestID, workerName));
            updateRequest(requestID, new Update().set("status", "PROCESSING").set("assignedTo", workerName));

            clearTask(requestID);

            return request;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


    // util func

    private void updateRequest(String requestID, Update u) {

        Query q = new Query(Criteria.where("_id").is(new ObjectId(requestID)));
        UpdateResult result = template.updateFirst(q, u, GyConstant.REQUESTS_COLLECTION);
        String note = u.toString();
        if (result.getModifiedCount() == 1) {
            logger.info(String.format("Updated Request #%s :: %s", requestID, note));
        } else {
            logger.warn(String.format("Request #%s is not updated :: %s", requestID, note));
        }

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
        if (d.getDeletedCount() == 0)
            logger.warn(String.format("Task #%s was not removed from Tasks Collection", requestID));
        else
            logger.info(String.format("Task #%s was removed from Tasks Collection", requestID));

    }

}
