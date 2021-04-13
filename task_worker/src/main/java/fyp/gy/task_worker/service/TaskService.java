package fyp.gy.task_worker.service;

import fyp.gy.common.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Timer;
import java.util.TimerTask;

@Service
public class TaskService {

    Logger logger = LoggerFactory.getLogger(TaskService.class);

    // Autowired
    private final DockerService docker;
    private final RestTemplate restTemplate;

    // internal use
    private final String pollURL;

//    private Thread pollingThread = new Thread(() -> {
//        while (true) {
//            logger.info("Checking for job in queue...");
//            try {
//                Task task = jobQueue.take();
//                try {
//                    logger.info(String.format("Request #%s polled, looking for machine available...", task.getRequestID()));
//                    processRequest(task.getRequestID());
//                } catch (Exception e) {
//                    jobQueue.add(task);
//                    e.printStackTrace();
//                }
//            } catch (InterruptedException e) {
//                logger.error("fail polling request (QueueService): " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//    });

    @Autowired
    public TaskService(DockerService docker,
                       @Value("${mainserver.hostname}") String hn,
                       @Value("${mainserver.port}") String port,
                       @Value("${spring.application.name}") String workerID
    ) {

        this.docker = docker;
        this.restTemplate = new RestTemplate();

        String msURL = String.format("http://%s:%s", hn, port);
        this.pollURL = msURL + "/request/poll?workerID=" + workerID;

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                pollAndRun();
            }
        }, 10000, 10000);

        //<editor-fold desc="Manually run a docker on startup">
//        logger.info(String.format("MainServer : %s:%s", ip_addr, port));
//
//
//        String downloadURL = "http://192.168.0.121:8080/files/603fe0cd0407ac653c0d0900";
//
//        try {
//            String containerID = dockerClient.createContainerCmd("zz:proto")
//                    .withHostConfig(hostConfig)
//                    .withStdinOpen(true)
//                    .withEnv(
//                            String.format("inputParam=%s", "ewogICJiYXRjaF9zaXplIjogMzIsCiAgImVwb2NocyI6IDEwLAogICJzZWVkIjogNDIsCiAgInZhbGlkYXRpb25fc3BsaXQiOiAwLjIsCiAgIm1heF9mZWF0dXJlcyI6IDEwMDAwLAogICJzZXF1ZW5jZV9sZW5ndGgiOiAyNTAsCiAgImVtYmVkZGluZ19kaW0iOiAxNiwKICAibG9zcyI6ICJiaW5hcnlfY3Jvc3NlbnRyb3B5IiwKICAib3B0aW1pemVyIjogImFkYW0iCn0"),
//                            String.format("downloadURL=%s", downloadURL),
//                            String.format("uploadURL=%s", uploadURL)
//                    ).exec().getId();
//
//            System.out.println("done creating docker");
//            dockerClient.startContainerCmd(containerID).exec();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //</editor-fold>
    }

    private void pollAndRun() {

        logger.info("Polling job from main server");
        try {
            Request request = restTemplate.getForObject(pollURL,Request.class);
            if(request!=null) docker.process(request);
        } catch (RestClientException e) {
//            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }


}

//<editor-fold desc="ancient code">
//        String containerID = dockerClient.createContainerCmd("zz:proto2")
//                .withHostConfig(hostConfig)
//                .withEnv(
//                        String.format("inputParam=%s", inputParam),
//                        String.format("downloadURL=%s", downloadURL),
//                        String.format("uploadURL=%s", uploadURL))
////                .withCmd("bash")
////                .withStdinOpen(true)
//                .exec().getId();
//
//        dockerClient.startContainerCmd(containerID).exec();
//</editor-fold>