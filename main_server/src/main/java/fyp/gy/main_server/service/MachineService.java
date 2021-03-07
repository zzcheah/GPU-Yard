package fyp.gy.main_server.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Service
public class MachineService {

    private final Map<String, Machine> map;
    private final BlockingQueue<String> queue;
    private final EurekaClient eurekaClient;

    Logger logger = LoggerFactory.getLogger(MachineService.class);

    public MachineService(@Qualifier("eurekaClient") EurekaClient eurekaClient) {
        this.map = new HashMap<>();
        this.queue = new LinkedBlockingDeque<>();
        this.eurekaClient = eurekaClient;

        retrieveTaskWorkers();
    }



    public void registerMachine(String machineID) {

        InstanceInfo serv = eurekaClient.getApplication(machineID).getInstances().get(0);

        String base_URL = String.format("http://%s:%s", serv.getIPAddr(), serv.getPort());
        WebClient client = WebClient.create(base_URL);

        Machine machine = new Machine();
        machine.setWebClient(client);

        map.put(machineID, machine);

        logger.info(String.format("Registered Machine: %s", machineID));

    }

    public void releaseMachine(String machineID) {
        queue.add(machineID);
    }

    public void allocateMachine(String requestID) throws Exception {

        boolean assigned = false;

        while(!assigned) {

            String machineID = queue.take();
            WebClient client = map.get(machineID).getWebClient();

            // attempt assigning to machine
            String res = client.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/process/")
                            .queryParam("requestID", requestID)
                            .build())
                    .retrieve().bodyToMono(String.class).block();

            logger.info(res);
            assigned = true;

        }
    }

    private void retrieveTaskWorkers() {
        logger.info("Retrieving Task Workers");
        eurekaClient.getApplications().getRegisteredApplications().forEach(application -> {
            if(!application.getName().equals("MAIN_SERVER")) {
                String machineID = application.getName();
                registerMachine(machineID);
            }
        });
    }

    public static class Machine {

        private WebClient webClient;

        public WebClient getWebClient() {
            return webClient;
        }

        public void setWebClient(WebClient webClient) {
            this.webClient = webClient;
        }

    }

}
