package fyp.gy.main_server.service;

import fyp.gy.common.constant.GyConstant;
import fyp.gy.common.model.Request;
import fyp.gy.main_server.model.User;
import fyp.gy.main_server.model.Worker;
import fyp.gy.main_server.repository.RequestRepository;
import fyp.gy.main_server.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class WorkerService {

    Logger logger = LoggerFactory.getLogger(User.class);

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
            workerMap.put(worker.getName(), worker);
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                updateStates();
            }
        }, DURATION, DURATION);
    }

    public boolean allowPolling(String workerName) {

        Worker tmpWorker = workerRepo.findByName(workerName).orElse(null);
        if (tmpWorker == null) {
            logger.error("Unrecognized Worker " + workerName + " attempted to poll request!!");
            return false;
        }

        if(!workerMap.containsKey(workerName)) {
            workerMap.put(workerName, tmpWorker);
        }

        Worker worker = workerMap.get(workerName);

        // update last active
        worker.setStatus("Active");
        worker.setLastActive(new Date(System.currentTimeMillis()));

        return worker.getRunningTasks().size() < worker.getMaxTasks();
    }

    public void addToWorkerTasks(String workerName, String requestID) {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(workerName));
        Update update = new Update();
        update.addToSet("runningTasks", requestID);
        template.updateFirst(query, update, GyConstant.WORKERS_COLLECTION);
        workerMap.get(workerName).getRunningTasks().add(requestID);
    }

    private void updateStates() {

        Date currentTime = new Date(System.currentTimeMillis());
        workerMap.forEach((name, worker) -> {

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
                    logger.error(String.format("RequestID #%s for Worker %s is missing", requestID, worker.getName()));
                    continue;
                }

                if (!request.getStatus().equals("PROCESSING")) {
                    worker.getRunningTasks().remove(i--);
                }
            }

            template.save(worker,GyConstant.WORKERS_COLLECTION);
        });

    }



}
