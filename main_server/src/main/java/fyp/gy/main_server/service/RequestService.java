package fyp.gy.main_server.service;

import com.mongodb.client.result.DeleteResult;
import fyp.gy.common.constant.GyConstant;
import fyp.gy.common.model.Notification;
import fyp.gy.common.model.Request;
import fyp.gy.main_server.model.Task;
import fyp.gy.main_server.repository.DockerImageRepository;
import fyp.gy.main_server.repository.RequestRepository;
import fyp.gy.main_server.resolver.input.CreateRequestInput;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import static fyp.gy.common.constant.GyConstant.DATE_TIME_FORMAT;

@Service
@Slf4j
public class RequestService {

    private final MongoTemplate template;
    private final WorkerService workerService;
    private final RequestRepository requestRepo;
    private final DockerImageRepository imageRepo;
    public final PriorityQueue<Task> jobQueue = new PriorityQueue<>();

    public RequestService(MongoTemplate template, WorkerService workerService, RequestRepository requestRepo, DockerImageRepository imageRepo) {
        this.template = template;
        this.workerService = workerService;
        this.requestRepo = requestRepo;
        this.imageRepo = imageRepo;
        restoreQueue();
    }

    // Correspond to Controller
    // depreciated
    public Request addRequest(Map<String, Object> payload) {

        @SuppressWarnings("unchecked") Request detail = Request.builder()
                .status("NEW")
                .name((String) payload.get("name"))
                .userID((String) payload.get("userID"))
                .createdAt((String) payload.get("createdAt"))
                .title((String) payload.get("title"))
                .image((String) payload.get("image"))
                .encodedParam((String) payload.get("param"))
                .inputFiles((List<String>) payload.get("inputFiles"))
                .build();

        detail = template.insert(detail, GyConstant.REQUESTS_COLLECTION);

        Task task = new Task();
        task.setRequestID(detail.getId());
        task.setCreatedAt(detail.getCreatedAt());

        // Record in mongo for persistency
        template.insert(task, GyConstant.TASKS_COLLECTION);

        // Volatile job queue
        jobQueue.add(task);
        log.info("Added request #" + detail.getId() + " to job queue");
        return detail;
    }

    public Request createRequest(CreateRequestInput input, String uid) {

//        DockerImage image = imageRepo.findByName(input.getImage()).orElse(null);
//        if(image==null) throw new GraphQLException("Invalid Docker Image, image does not exist in database");

        Request request = Request.builder()
                .status("NEW")
                .name(input.getName())
                .userID(uid)
                .createdAt(getTime())
                .title("Untitled")
                .image(input.getImage())
                .encodedParam(input.getParam())
                .inputFiles(input.getInputFiles())
                .build();

        request = template.save(request);

        Task task = new Task();
        task.setRequestID(request.getId());
        task.setCreatedAt(request.getCreatedAt());

        // Record in mongo for persistency
        task = template.save(task);

        // Volatile job queue
        jobQueue.add(task);
        log.info("Added request #" + request.getId() + " to job queue");

        return request;
    }

    public Request getJob(String workerName) {

        try {

            if (!workerService.allowPolling(workerName)) return null;

            log.info(String.format("Worker #%s pooling job queue", workerName));

            Task task = jobQueue.poll();
            if (task == null) return null;

            String requestID = task.getRequestID();
            Request request = template.findById(new ObjectId(requestID), Request.class, GyConstant.REQUESTS_COLLECTION);

            if (request == null) {
                log.warn(String.format("Cannot locate request #%s in db", requestID));
                return null;
            }

            workerService.addToWorkerTasks(workerName, requestID);

            log.info(String.format("Request #%s is assigned to Worker #%s", requestID, workerName));

            Query q = new Query(Criteria.where("_id").is(new ObjectId(requestID)));
            Update u = new Update().set("status", "PROCESSING").set("assignedTo", workerName);
            template.updateFirst(q, u, GyConstant.REQUESTS_COLLECTION);

            clearTask(requestID);

            return request;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public String completeRequest(String requestID, String status, String remark, String fileID) {
        Request request = template.findById(new ObjectId(requestID),Request.class);
        if(request==null) throw new NullPointerException("Missing Request # "+ requestID);

        request.setStatus(status);
        request.setRemark(remark);
        if(fileID!=null) {
            request.getOutputFiles().add(fileID);
        }

        requestRepo.save(request);

        log.info(String.format("Request #%s updated, Status: %s.", requestID, status));

        // notify user
        Notification notification = Notification.builder()
                .content(String.format("Your request \"%s\" has completed.",request.getName()))
                .isRead(false)
                .severity("info")
                .user(request.getUserID())
                .createdAt(getTime())
                .link("/requests/"+requestID)
                .build();

        template.save(notification);
        return "Request completed.";
    }



    // util func

    private void restoreQueue() {

        List<Task> jobRestored = template.findAll(Task.class, GyConstant.TASKS_COLLECTION);
//        Collections.sort(jobRestored);

        if (jobRestored.size() != 0) {
            jobQueue.addAll(jobRestored);
            log.info(String.format("Restored %d Tasks", jobRestored.size()));
        }

    }

    private void clearTask(String requestID) {

        Query q = new Query(Criteria.where("requestID").is(requestID));
        DeleteResult d = template.remove(q, GyConstant.TASKS_COLLECTION);
        if (d.getDeletedCount() == 0)
            log.warn(String.format("Task #%s was not removed from Tasks Collection", requestID));
        else
            log.info(String.format("Task #%s was removed from Tasks Collection", requestID));

    }

    private String getTime() {
        Date currentTime = new Date(System.currentTimeMillis());
        return new SimpleDateFormat(DATE_TIME_FORMAT, Locale.ENGLISH).format(currentTime);
    }
}
