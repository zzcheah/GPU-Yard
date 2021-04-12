package fyp.gy.main_server.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.xml.ws.http.HTTPException;
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

        Machine machine = new Machine();
        WebClient client = WebClient.create(base_URL);
        machine.setWebClient(client);

        map.put(machineID, machine);
        logger.info(String.format("Registered Machine: %s", machineID));
    }

    public void releaseMachine(String machineID) {
        logger.info("Free-ing machine");
        queue.add(machineID);
    }

    public void allocateMachine(String requestID) throws Exception {

        int retries = 1;

        while (retries>0) {

            String machineID = queue.take();
            logger.info(String.format("Sending request #%s to Machine #%s",requestID,machineID));

            WebClient client = map.computeIfAbsent(machineID, k ->{
                registerMachine(machineID);
                return map.get(machineID);
            }).getWebClient();

            // attempt assigning to machine
            Mono<String> res = client.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/process/")
                            .queryParam("requestID", requestID)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> clientResponse.bodyToMono(String.class).flatMap(body-> Mono.error(new Exception(body))))
                    .bodyToMono(String.class);

            try {
                String blocked = res.block();
                logger.info("Task Worker Response: "+blocked);
                break;
            } catch (Exception e) {
                logger.error(String.format("Fail assigning request #%s to Machine #%s",requestID,machineID));
                logger.error(e.getMessage());
                retries--;
            }





        }
    }

    private void retrieveTaskWorkers() {
        logger.info("Retrieving Task Workers");
        eurekaClient.getApplications().getRegisteredApplications().forEach(application -> {
            if (!application.getName().equals("MAIN_SERVER")) {
                String machineID = application.getName();
                registerMachine(machineID);
                releaseMachine(machineID);
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
