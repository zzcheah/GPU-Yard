package fyp.gy.main_server.service;

import com.mongodb.client.result.UpdateResult;
import fyp.gy.common.constant.GyConstant;
import fyp.gy.common.model.Notification;
import fyp.gy.common.model.Request;
import fyp.gy.main_server.model.Worker;
import fyp.gy.main_server.repository.RequestRepository;
import fyp.gy.main_server.repository.WorkerRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static fyp.gy.common.constant.GyConstant.DATE_TIME_FORMAT;

@Service
@Slf4j
public class WorkerService {

    private final MongoTemplate template;
    private final WorkerRepository workerRepo;
    private final RequestRepository requestRepo;
    private final Map<String, Worker> workerMap;
    private final static long DURATION =  TimeUnit.MINUTES.toMillis(5);

    public WorkerService(MongoTemplate template, WorkerRepository workerRepo, RequestRepository requestRepo) {
        this.template = template;
        this.workerRepo = workerRepo;
        this.requestRepo = requestRepo;
        this.workerMap = new HashMap<>();

        List<Worker> workerList = template.findAll(Worker.class);
        for (Worker worker : workerList) {
            worker.setStatus("Checking");
            workerMap.put(worker.getId(), worker);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                updateStates();
            }
        }, DURATION, DURATION);
    }

    public boolean allowPolling(String workerId) {

        Worker tmpWorker = workerRepo.findById(workerId).orElse(null);
        if (tmpWorker == null) {
            log.error("Unrecognized Worker #" + workerId + " attempted to poll request!!");
            return false;
        }

        if(!workerMap.containsKey(workerId)) {
            workerMap.put(workerId, tmpWorker);
        }

        Worker worker = workerMap.get(workerId);

        // update last active
        worker.setStatus("Active");
        worker.setLastActive(new Date(System.currentTimeMillis()));

        return worker.getRunningTasks().size() < worker.getMaxTasks();
    }

    public void addToWorkerTasks(String workerId, String requestID) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(new ObjectId(workerId)));
        Update update = new Update();
        update.addToSet("runningTasks", requestID);
        template.updateFirst(query, update, GyConstant.WORKERS_COLLECTION);
        workerMap.get(workerId).getRunningTasks().add(requestID);
    }

    public List<Worker> getWorkerList(){
        return new ArrayList<>(workerMap.values());
    }

    public Boolean verifyWorker(String id) {
        Worker worker = workerRepo.findById(id).orElse(null);
        return worker!=null;
    }
    public void addToMap(Worker worker) {
        workerMap.put(worker.getId(),worker);
    }

    private void updateStates() {

        Date currentTime = new Date(System.currentTimeMillis());
        workerMap.forEach((id, worker) -> {

            // calculate last seen, if more than 5 mins, set status to inactive
            if(worker.getLastActive()==null || currentTime.getTime()-worker.getLastActive().getTime()>DURATION) {
                worker.setStatus("Inactive");
            }

            // remove tasks that are no longer processing
            List<String> runningTasks = worker.getRunningTasks();
            for (int i = 0; i < runningTasks.size(); i++) {
                String requestID = runningTasks.get(i);
                Request request = requestRepo.findById(requestID).orElse(null);

                if (request == null) {
                    log.warn(String.format("RequestID #%s for Worker $%s is missing", requestID, worker.getId()));
                    worker.getRunningTasks().remove(i--);
                    continue;
                }

                if (!request.getStatus().equals("PROCESSING")) {
                    worker.getRunningTasks().remove(i--);
                } else if(worker.getStatus().equals("Inactive")){
                    Query q = new Query(Criteria.where("_id").is(new ObjectId(requestID)));
                    Update u = new Update();
                    u.set("status","ERROR");
                    u.set("remark", String.format("Worker #%s shut down unexpectedly. Contact Admin for more info", worker.getId() ));
                    UpdateResult result = template.updateFirst(q, u, GyConstant.REQUESTS_COLLECTION);
                    String note = u.toString();
                    if (result.getModifiedCount() == 1) {
                        log.info(String.format("Updated Request #%s :: %s", requestID, note));
                        worker.getRunningTasks().remove(i--);
                        Notification notification = Notification.builder()
                                .content(String.format("Error processing your request # %s.",request.getName()))
                                .isRead(false)
                                .severity("error")
                                .user(request.getUserID())
                                .link("/requests/"+requestID)
                                .createdAt(new SimpleDateFormat(DATE_TIME_FORMAT, Locale.ENGLISH).format(currentTime))
                                .build();

                        template.save(notification);
                    } else {
                        log.warn(String.format("Request #%s is not updated :: %s", requestID, note));
                    }
                }
            }

            template.save(worker,GyConstant.WORKERS_COLLECTION);
        });

    }



}
