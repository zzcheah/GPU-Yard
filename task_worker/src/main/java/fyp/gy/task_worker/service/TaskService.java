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

import java.io.IOException;

@Service
public class TaskService {

    Logger logger = LoggerFactory.getLogger(TaskService.class);

    // Autowired
    private final DockerClient dockerClient;
    private final HostConfig hostConfig;
    private final MongoTemplate template;

    // internal use
    private final String downloadPrefix;
    private final String uploadURL;

    public TaskService(
            MongoTemplate template,
            DockerClient dockerClient,
            @Qualifier("eurekaClient") EurekaClient eurekaClient) throws Exception {

        this.template = template;
        this.dockerClient = dockerClient;

        dockerClient.pingCmd();
        hostConfig = new HostConfig()
                .withRuntime("nvidia")
                .withNetworkMode("host");

        InstanceInfo serv;

        try {
            serv = eurekaClient.getApplication("MAIN_SERVER").getInstances().get(0);
        } catch (Exception e) {
            throw new Exception("MainServer not running");
        }

        String ip_addr = serv.getIPAddr();
        int port = serv.getPort();
        this.downloadPrefix = String.format("http://%s:%d/files/", ip_addr, port);
        this.uploadURL = String.format("http://%s:%d/files/add", ip_addr, port);
        logger.info(String.format("MainServer : %s:%s", ip_addr, port));


        String downloadURL = "http://192.168.0.121:8080/files/603fe0cd0407ac653c0d0900";

        try {
            String containerID = dockerClient.createContainerCmd("zz:proto")
                    .withHostConfig(hostConfig)
                    .withStdinOpen(true)
                    .withEnv(
                            String.format("inputParam=%s", "ewogICJiYXRjaF9zaXplIjogMzIsCiAgImVwb2NocyI6IDEwLAogICJzZWVkIjogNDIsCiAgInZhbGlkYXRpb25fc3BsaXQiOiAwLjIsCiAgIm1heF9mZWF0dXJlcyI6IDEwMDAwLAogICJzZXF1ZW5jZV9sZW5ndGgiOiAyNTAsCiAgImVtYmVkZGluZ19kaW0iOiAxNiwKICAibG9zcyI6ICJiaW5hcnlfY3Jvc3NlbnRyb3B5IiwKICAib3B0aW1pemVyIjogImFkYW0iCn0"),
                            String.format("downloadURL=%s", downloadURL),
                            String.format("uploadURL=%s", uploadURL)
                    ).exec().getId();

            System.out.println("done creating docker");
            dockerClient.startContainerCmd(containerID).exec();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void process(String id) throws IOException {

        Request req = template.findById(id, Request.class, GyConstant.REQUESTS_COLLECTION);
        if (req == null) throw new IOException(String.format("Cannot locate request #%s in db", id));

//        String downloadURL = "http://192.168.0.121:8080/files/603fe0cd0407ac653c0d0900";
//        String uploadURL = "http://192.168.0.121:8080/files/add";

        String downloadURL = downloadPrefix + req.getInputFiles().get(0);
        String encodedJson = req.getEncodedParam();

        System.out.println(downloadURL);
        System.out.println(uploadURL);
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

//        dockerClient.copyArchiveToContainerCmd(containerID)
//                .withHostResource("")
//                .exec();


    }


}

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