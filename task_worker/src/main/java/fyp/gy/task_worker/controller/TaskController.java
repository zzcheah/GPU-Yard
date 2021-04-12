package fyp.gy.task_worker.controller;

import fyp.gy.task_worker.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class TaskController {


    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // title requestID
    @PostMapping("/process")
    public String processRequest(
            @RequestParam("requestID") String requestID) {
        try {
            taskService.process(requestID);
            return "Request #" + requestID + " is being processed";
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Failed to process Request #" + requestID + " :: "+ex.getMessage());
        }
    }

}
