package fyp.gy.task_worker.service;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.DeviceRequest;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.google.common.collect.ImmutableList;
import fyp.gy.common.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DockerService {

    Logger logger = LoggerFactory.getLogger(DockerService.class);

    private static final String OS = System.getProperty("os.name").toLowerCase();
    public static boolean IS_UNIX = (OS.contains("nix") || OS.contains("nux") || OS.indexOf("aix") > 0);
    public static boolean IS_WINDOWS = (OS.contains("win"));

    private final DockerClient dockerClient;
    private final HostConfig hostConfig;

    private final String downloadPrefix;
    private final String uploadURL;

    private final List<String> imageList = new ArrayList<>();


    @Autowired
    public DockerService(@Value("${mainserver.hostname}") String hn,
                         @Value("${mainserver.port}") String port
    ) {

        String dockerEndpoint = null;

        if (IS_UNIX) dockerEndpoint = "unix:///var/run/docker.sock";
        else if (IS_WINDOWS) dockerEndpoint = "npipe:////./pipe/docker_engine";

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(Objects.requireNonNull(dockerEndpoint)).build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);

        dockerClient.pingCmd();

        hostConfig = new HostConfig()
                .withNetworkMode("host")
                .withDeviceRequests(
                        ImmutableList.of(
                                new DeviceRequest().withCapabilities(ImmutableList.of(ImmutableList.of("gpu")))
                        ));


        String msURL = String.format("http://%s:%s", hn, port);
        this.downloadPrefix = msURL + "/files/";
        this.uploadURL = msURL + "/request/complete";

        retrieveImages();
    }

    public void process(Request req) {

        String id = req.getId();
        logger.info("Processing request #" + id);

        String downloadURL = downloadPrefix + req.getInputFiles().get(0);
        String encodedJson = req.getEncodedParam();

//        System.out.println(downloadURL);
//        System.out.println(uploadURL);
        logger.info(req.getImage());

        if(!imageList.contains(req.getImage())) {
            try {
                logger.info("Image not found, pulling from docker hub");
                pullImage(req.getImage());
                logger.info("Successfully pulled image!");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String containerID = dockerClient.createContainerCmd(req.getImage())
                .withHostConfig(hostConfig)
                .withStdinOpen(true)
                .withEntrypoint("python", "/gy/gyrun.py")
                .withEnv(
                        String.format("inputParam=%s", encodedJson),
                        String.format("downloadURL=%s", downloadURL),
                        String.format("uploadURL=%s", uploadURL),
                        String.format("requestID=%s", req.getId())
                ).exec().getId();

        dockerClient.startContainerCmd(containerID).exec();
        logger.info(String.format("Container #%s is processing request #%s", containerID, id));

//        dockerClient.copyArchiveToContainerCmd(containerID)
//                .withHostResource("")
//                .exec();


    }

    private void pullImage(String imageWithTag) throws InterruptedException {

        String[] temp = imageWithTag.split(":");
        String image = temp[0];
        String tag = temp[1];

        dockerClient.pullImageCmd(image)
                .withTag(tag)
                .exec(new PullImageResultCallback())
                .awaitCompletion();

        imageList.add(imageWithTag);
    }

    private void retrieveImages() {
        List<Image> images = dockerClient.listImagesCmd().exec();

        for (Image image : images) {
            imageList.addAll(Arrays.asList(image.getRepoTags()));
        }
    }


    //<editor-fold desc="ancient code">

//
//    private String runContainerWithImage(String imageWithTag) throws Exception {
//        String id = dockerClient.createContainerCmd(imageWithTag)
//                .withHostConfig(new HostConfig().withRuntime("nvidia"))
//                .exec().getId();
//
//        dockerClient.startContainerCmd(id).exec();
//        return id;
//    }
//
//    public List<Container> listContainers() {
//        return dockerClient.listContainersCmd()
//                .withShowSize(true)
//                .withShowAll(true)
//                .withStatusFilter(Collections.singleton("exited")).exec();
//    }
//
//    private Ports bindPort(int exposedPort, int bindPort) {
//        ExposedPort ep = ExposedPort.tcp(exposedPort);
//        Ports portBinding = new Ports();
//        portBinding.bind(ep, Ports.Binding.bindPort(bindPort));
//        return portBinding;
//
////            ExposedPort tcp8888 = ExposedPort.tcp(8888);
////            Ports portBinding = new Ports();
////            portBinding.bind(tcp8888, Ports.Binding.bindPort(8888));
////
////
////            containerId = dockerClient.createContainerCmd(imageName+":"+tag)
////                    .withHostConfig(new HostConfig().withPortBindings(portBinding).withRuntime("nvidia"))
////                    .withExposedPorts(tcp8888)
////                    .exec().getId();
//
//    }


    //        try {
//            String env_Var = "inputParam";
//
//            String imageWithTag = "tensorflow/tensorflow:latest-gpu";
//            HostConfig hostConfig = new HostConfig()
//                    .withRuntime("nvidia")
//                    .withNetworkMode("host");
//
//            logger.info("docker service running");
//            dockerClient.pingCmd().exec();
//
////            String id = dockerClient.createContainerCmd(imageWithTag)
////                    .withHostConfig(hostConfig)
//////                    .withCmd("bash")
////                    .withStdinOpen(true)
//////                    .withEnv(String.format("%s=%s",env_Var,encodedJson))
////                    .exec().getId();
////
////            dockerClient.startContainerCmd(id).exec();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    //</editor-fold>

}
