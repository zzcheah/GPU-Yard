package fyp.gy.task_worker.controller;

import fyp.gy.task_worker.service.TaskService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskController {


    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // title requestID
    @PostMapping("/process")
    public String processRequest(
            @RequestParam("title") String title,
            @RequestParam("requestID") String requestID) {
        try {
            taskService.process(title, requestID);
            return "Request #" + requestID + " (" + title + ")" + " is being processed";
        } catch (Exception e) {
            return "Failed to process Request #" + requestID + " (" + title + ")";
        }
    }

}