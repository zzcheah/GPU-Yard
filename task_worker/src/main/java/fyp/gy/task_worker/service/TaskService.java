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

    private final DockerClient dockerClient;
    private final HostConfig hostConfig;
    private final EurekaClient eurekaClient;

    private final MongoTemplate template;
    private final String downloadPrefix;
    private final String uploadURL;

    public TaskService(
            MongoTemplate template,
            DockerClient dockerClient,
            @Qualifier("eurekaClient")
                    EurekaClient eurekaClient
    ) {
        this.template = template;
        this.eurekaClient = eurekaClient;
        this.dockerClient = dockerClient;

        dockerClient.pingCmd();
        hostConfig = new HostConfig()
                .withRuntime("nvidia")
                .withNetworkMode("host");

        InstanceInfo app = eurekaClient.getApplication("MAIN_SERVER").getInstances().get(0);
        String ip_addr = app.getIPAddr();
        int port = app.getPort();
        this.downloadPrefix =  String.format("http://%s:%d/files/",ip_addr,port);
        this.uploadURL = String.format("http://%s:%d/files/add",ip_addr,port);
        logger.info(String.format("MainServer : %s:%s", ip_addr,port));

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