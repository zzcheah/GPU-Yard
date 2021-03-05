package fyp.gy.main_server.controller;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import fyp.gy.main_server.service.QueueService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;


@RestController
public class QueueController {

    private final QueueService queueService;
    private final EurekaClient eurekaClient;

    public QueueController(QueueService queueService, @Qualifier("eurekaClient") EurekaClient eurekaClient) {
        this.queueService = queueService;
        this.eurekaClient = eurekaClient;
    }

    @PostMapping("/queue/add")
    public String addRequest(@RequestBody Map<String, Object> payload) {


        try {
            return queueService.addRequest(payload);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e
            );
        }
    }

    @PostMapping("/machine/release")
    public void releaseMachine(
            @RequestParam("machineID") String machineID,
            @RequestParam("requestID") String requestID) {
        try {
            queueService.freeMachine(machineID, requestID, "COMPLETED");
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e
            );
        }
    }

    @GetMapping("/foo")
    public String foo() {

        RestTemplate restTemplate = new RestTemplate();
        InstanceInfo app = eurekaClient.getApplication("CRAZY_PIG").getInstances().get(0);
        String url =String.format("http://%s:%s/process",app.getIPAddr(),app.getPort());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("requestID", "fasdasm");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity( url, request , String.class );
        String obj = response.getBody();
        System.out.println(obj);
        return obj;
    }

}
