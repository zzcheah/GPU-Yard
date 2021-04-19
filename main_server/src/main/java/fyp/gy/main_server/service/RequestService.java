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
import java.util.PriorityQueue;

@Service
public class RequestService {

    Logger logger = LoggerFactory.getLogger(RequestService.class);

    private final MongoTemplate template;
    public final PriorityQueue<Task> jobQueue = new PriorityQueue<>();

    public RequestService(MongoTemplate template) {
        this.template = template;
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

    public void completeRequest(String requestID, String remark) {

        try {
            Update update = new Update();
            update.set("status", "COMPLETED");
            update.set("remark", remark);

            updateRequest(requestID,update);

            clearTask(requestID);
            logger.info(String.format("Request #%s completed.", requestID));

            // notify user;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Request getJob(String workerID) {

        try {
            logger.info(String.format("Worker #%s pooling job queue", workerID));
            Task task = jobQueue.poll();
            if(task==null) return null;

            String requestID = task.getRequestID();
            Request request = template.findById(new ObjectId(requestID),Request.class, GyConstant.REQUESTS_COLLECTION);

            // TODO: better handle missing request
            if(request == null) {
                logger.warn(String.format("Cannot locate request #%s in db", requestID));
                return null;
            }

            logger.info(String.format("Request #%s is assigned to Worker #%s",requestID,workerID));
            updateRequest(requestID,new Update().set("status", "PROCESSING"));
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
        if(result.getModifiedCount()==1) {
            logger.info(String.format("Updated Request #%s :: %s", requestID, note));
        } else {
            logger.warn(String.format("Request #%s is not updated :: %s",requestID, note));
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
