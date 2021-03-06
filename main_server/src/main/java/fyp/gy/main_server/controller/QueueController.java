package fyp.gy.main_server.controller;

import fyp.gy.main_server.service.QueueService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;


@RestController
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
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

}

//    @GetMapping("/foo")
//    public String foo() {
//
//        RestTemplate restTemplate = new RestTemplate();
//        InstanceInfo app = eurekaClient.getApplication("CRAZY_PIG").getInstances().get(0);
//        String url =String.format("http://%s:%s/process",app.getIPAddr(),app.getPort());
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//
//        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
//        map.add("requestID", "fasdasm");
//
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
//
//        ResponseEntity<String> response = restTemplate.postForEntity( url, request , String.class );
//        String obj = response.getBody();
//        System.out.println(obj);
//        return obj;
//    }