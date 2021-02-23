package fyp.gy.main_server.controller;


import fyp.gy.main_server.model.RequestFile;
import fyp.gy.main_server.service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> getFile(@PathVariable String id){

        try {
            RequestFile file = fileService.getFile(id);
            InputStreamResource resource = file.getResource();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.noContent().build();
        }



    }

    @PostMapping("/files/add")
    public String addFile(@RequestParam("file") MultipartFile file)  {
        try {
            return fileService.addFile(file);
        } catch (IOException e) {
            return null;
        }
    }




}
