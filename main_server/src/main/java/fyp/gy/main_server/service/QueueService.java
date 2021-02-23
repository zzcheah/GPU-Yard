package fyp.gy.main_server.service;

import fyp.gy.main_server.model.RecordCollection;
import fyp.gy.main_server.model.RequestDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class QueueService {

    Logger logger = LoggerFactory.getLogger(QueueService.class);

    private final MongoTemplate template;

    BlockingQueue<String> jobQueue = new LinkedBlockingDeque<>();
    BlockingQueue<String> machineQueue = new LinkedBlockingDeque<>();

    Thread pollingThread = new Thread(() -> {
        while (true) {
            logger.info("Polling...");
            try {
                String id = jobQueue.take();
                assignJob(id);
            } catch (InterruptedException e) {
                logger.error("fail polling request (QueueService)");
            }
        }
    });

    public QueueService(MongoTemplate template) {
        this.template = template;
        pollingThread.start();
    }

    // TODO: convert json or anything to RequestDetail
    public void addRequest(String id, String title) {

        Query query = new Query();
        query.addCriteria(Criteria.where("title").is(title));

        if (!template.exists(query, "RecordCollection")) {
            RecordCollection recordCollection = new RecordCollection();
            recordCollection.setTitle(title);
            template.insert(recordCollection, "RecordCollection");
        }

        Update update = new Update();
        RequestDetail detail = new RequestDetail();
        detail.setX("asd");
        detail.setY("asd");
        update.push("requests",detail);
        // TODO: mark status as 'NEW'
        template.updateFirst(query,update,"RecordCollection");


        jobQueue.add(id);
        logger.info("Added request #" + id + " to job queue");
        // TODO: record the request to mongoDB (Queue Collection)
    }

    public void freeMachine(String id) {
        // TODO: get completed request ID
        // TODO: update the request to 'COMPLETED or ERROR'
        // TODO: notify user
        logger.info("Free-ing machine #" + id);
        machineQueue.add(id);
    }

    private void assignJob(String request_id) {
        try {

            String assignedMachine = machineQueue.take();
            // TODO: update request status to 'PROCESSING'
            logger.info("Assigning request #" + request_id + " to machine #" + assignedMachine);
            // TODO: send API to ask machine to process the job
        } catch (InterruptedException e) {
            logger.error("fail allocating machine for request #" + request_id);
        }

    }


}
