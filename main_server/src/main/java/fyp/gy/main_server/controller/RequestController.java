package fyp.gy.main_server.controller;

import fyp.gy.common.model.Request;
import fyp.gy.main_server.service.FileService;
import fyp.gy.main_server.service.RequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;


@RestController
public class RequestController {

    private final RequestService requestService;
    private final FileService fileService;

    public RequestController(RequestService requestService, FileService fileService) {
        this.requestService = requestService;
        this.fileService = fileService;
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
    public ResponseEntity<Request> getJob(@RequestParam String worker) {
        try {
            Request request = requestService.getJob(worker);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e
            );
        }
    }

    @PostMapping("/request/complete")
    public String completeRequest(
            @RequestParam("requestID") String requestID,
            @RequestParam("status") String status,
            @RequestParam("remark") String remark,
            @RequestParam("file") MultipartFile file) {
        try {
            String fileID = fileService.addFile(file);
            return requestService.completeRequest(requestID, status, remark, fileID);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage(), e
            );
        }
    }

}
