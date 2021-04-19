package fyp.gy.main_server.controller;

import fyp.gy.common.model.Request;
import fyp.gy.main_server.service.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;


@RestController
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/request/add")
    public Request addRequest(@RequestBody Map<String, Object> payload) {
        try {
            return requestService.addRequest(payload);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e
            );
        }
    }

    // From task workers
    @GetMapping("/request/poll")
    public ResponseEntity<Request> getJob(@RequestParam String workerID) {
        try {
            Request request = requestService.getJob(workerID);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e
            );
        }
    }

    @PostMapping("/request/complete")
    public void completeRequest(@RequestParam("requestID") String requestID) {
        try {
            requestService.completeRequest(requestID, "some remark");
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e
            );
        }
    }

}
