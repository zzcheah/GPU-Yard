package fyp.gy.task_worker.service;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DockerService {

    Logger logger = LoggerFactory.getLogger(DockerService.class);

    public DockerService() {

        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("npipe:////./pipe/docker_engine").build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();


        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        dockerClient.pingCmd().exec();

        String imageName = "tensorflow/tensorflow";
//        String tag = "latest";
        String tag = "latest-gpu-py3-jupyter";
        String containerId = null;

        try {
//            System.out.println("Pulling Image: " + imageName);
//
//            dockerClient.pullImageCmd(imageName)
//                    .withTag(tag)
//                    .exec(new PullImageResultCallback())
//                    .awaitCompletion();

            logger.info("Pulled image: " + imageName);

            ExposedPort tcp8888 = ExposedPort.tcp(8888);
            Ports portBinding = new Ports();
            portBinding.bind(tcp8888, Ports.Binding.bindPort(8888));


            containerId = dockerClient.createContainerCmd(imageName+":"+tag)
//                    .withName("testTF")
//                    .withCmd("gpus")
                    .withHostConfig(new HostConfig().withPortBindings(portBinding).withRuntime("nvidia"))
                    .withExposedPorts(tcp8888)
                    .exec().getId();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (containerId != null) {
            dockerClient.startContainerCmd(containerId).exec();
        }


//        List<Container> containers = dockerClient.listContainersCmd()
//                .withShowSize(true)
//                .withShowAll(true)
//                .withStatusFilter(Collections.singleton("exited")).exec();
//
//        Container temp = containers.get(1);
//        System.out.println(temp.getId());
//        System.out.println(Arrays.toString(temp.getNames()));
//
//        System.out.println("running hello world container");
//
//        try {
//            dockerClient.startContainerCmd(temp.getId()).exec();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        dockerClient.listImagesCmd().exec().get(0).


    }


}
