package fyp.gy.task_worker.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import fyp.gy.common.constant.GyConstant;
import fyp.gy.common.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
public class TaskService {

    Logger logger = LoggerFactory.getLogger(TaskService.class);

    // Autowired
    private final DockerClient dockerClient;
    private final EurekaClient eurekaClient;
    private final HostConfig hostConfig;
    private final MongoTemplate template;

    // internal use
    private InstanceInfo mainServer;
    private String downloadPrefix;
    private String uploadURL;
    private String msURL;

    public TaskService(
            MongoTemplate template,
            DockerClient dockerClient,
            @Qualifier("eurekaClient") EurekaClient eurekaClient) {

        this.template = template;
        this.dockerClient = dockerClient;
        this.eurekaClient = eurekaClient;

        dockerClient.pingCmd();
        hostConfig = new HostConfig()
                .withRuntime("nvidia")
                .withNetworkMode("host");

        try {
            retrieveMainServer();
            RestTemplate restTemplate = new RestTemplate();
            String url = msURL.concat("request/complete?machineID=CRAZY_FROG&requestID=");
            restTemplate.postForObject(url,null,Void.class);

        } catch (Exception e) {
            logger.warn(e.getMessage());
        }

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

    public void process(String id) throws Exception {

        retrieveMainServer();
        logger.info("Processing request #"+id);

        Request req = template.findById(id, Request.class, GyConstant.REQUESTS_COLLECTION);
        if (req == null) throw new IOException(String.format("Cannot locate request #%s in db", id));

        String downloadURL = downloadPrefix + req.getInputFiles().get(0);
        String encodedJson = req.getEncodedParam();

//        System.out.println(downloadURL);
//        System.out.println(uploadURL);
        logger.info(req.getImage());

        String containerID = dockerClient.createContainerCmd(req.getImage())
                .withHostConfig(hostConfig)
                .withStdinOpen(true)
                .withEnv(
                        String.format("inputParam=%s", encodedJson),
                        String.format("downloadURL=%s", downloadURL),
                        String.format("uploadURL=%s", uploadURL)
                ).exec().getId();

        dockerClient.startContainerCmd(containerID).exec();
        logger.info(String.format("Container #%s is processing request #%s",containerID,id));

//        dockerClient.copyArchiveToContainerCmd(containerID)
//                .withHostResource("")
//                .exec();


    }

    private void retrieveMainServer() throws Exception {

        if(mainServer!=null) return;

        try {
            this.mainServer = eurekaClient.getApplication("MAIN_SERVER").getInstances().get(0);
            String ip_addr = mainServer.getIPAddr();
            int port = mainServer.getPort();
            this.downloadPrefix = String.format("http://%s:%d/files/", ip_addr, port);
            this.uploadURL = String.format("http://%s:%d/files/add", ip_addr, port);
            this.msURL = String.format("http://%s:%d", ip_addr, port);

        } catch (Exception e) {
            mainServer = null;
            throw new Exception("Unable to locate MainServer");
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