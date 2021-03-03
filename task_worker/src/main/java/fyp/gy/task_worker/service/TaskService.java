package fyp.gy.task_worker.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.HostConfig;
import fyp.gy.common.constant.GyConstant;
import fyp.gy.common.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TaskService {

    Logger logger = LoggerFactory.getLogger(TaskService.class);

    DockerClient dockerClient;
    HostConfig hostConfig;

    private final MongoTemplate template;

    public TaskService(MongoTemplate template, DockerClient dockerClient) {
        this.template = template;
        this.dockerClient = dockerClient;
        dockerClient.pingCmd();
        hostConfig = new HostConfig()
                .withRuntime("nvidia")
                .withNetworkMode("host");

        String downloadURL = "http://192.168.0.121:8080/files/603fe0cd0407ac653c0d0900";
        String uploadURL = "http://192.168.0.121:8080/files/add";
        String inputParam = "ewogICJiYXRjaF9zaXplIjogMzIsCiAgImVwb2NocyI6IDEwLAogICJzZWVkIjogNDIsCiAgInZhbGlkYXRpb25fc3BsaXQiOiAwLjIsCiAgIm1heF9mZWF0dXJlcyI6IDEwMDAwLAogICJzZXF1ZW5jZV9sZW5ndGgiOiAyNTAsCiAgImVtYmVkZGluZ19kaW0iOiAxNiwKICAibG9zcyI6ICJiaW5hcnlfY3Jvc3NlbnRyb3B5IiwKICAib3B0aW1pemVyIjogImFkYW0iCn0=";


        String containerID = dockerClient.createContainerCmd("zz:proto2")
                .withHostConfig(hostConfig)
                .withEnv(
                        String.format("inputParam=%s", inputParam),
                        String.format("downloadURL=%s", downloadURL),
                        String.format("uploadURL=%s", uploadURL))
//                .withCmd("bash")
//                .withStdinOpen(true)
                .exec().getId();

        dockerClient.startContainerCmd(containerID).exec();


    }

    public void process(String id) throws IOException {

        Request req = template.findById(id, Request.class, GyConstant.REQUESTS_COLLECTION);
        if (req == null) throw new IOException(String.format("Cannot locate request #%s in db", id));
        String encodedJson = req.getEncodedParam();

        String containerID = dockerClient.createContainerCmd(req.getImage())
                .withHostConfig(hostConfig)
                .withStdinOpen(true)
                .withEnv(String.format("inputParam=%s", encodedJson))
                .exec().getId();

        dockerClient.copyArchiveToContainerCmd(containerID)
                .withHostResource("")
                .exec();


    }


}
