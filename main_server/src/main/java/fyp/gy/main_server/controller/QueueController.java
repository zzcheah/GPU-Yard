package fyp.gy.main_server.controller;

import fyp.gy.main_server.service.QueueService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @PostMapping("/queue/add")
    public String addRequest(@RequestParam("id") String id, @RequestBody String json) {
        try {
            System.out.println(json);
            queueService.addRequest(id,"test-month");
            return "Successfully added request #" + id + " to job queue";
        } catch (Exception e) {
            return "Failed to add request #" + id + " to job queue";
        }
    }

    @PostMapping("/machine/release")
    public String releaseMachine(@RequestParam("id") String id) {
        try {
            queueService.freeMachine(id);
            return "Successfully released machine #" + id;
        } catch (Exception e) {
            return "Failed to release machine #" + id;
        }
    }

}
