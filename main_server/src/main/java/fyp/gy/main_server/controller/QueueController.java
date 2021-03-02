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
