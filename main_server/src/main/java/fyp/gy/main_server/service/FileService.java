package fyp.gy.main_server.service;

import com.mongodb.client.gridfs.model.GridFSFile;
import fyp.gy.main_server.model.RequestFile;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileService {

    Logger logger = LoggerFactory.getLogger(FileService.class);

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations operations;

    public FileService(GridFsTemplate gridFsTemplate, GridFsOperations operations) {
        logger.info("Starting File Service...");
        this.gridFsTemplate = gridFsTemplate;
        this.operations = operations;
    }


    public String addFile(MultipartFile file) throws IOException {
        logger.info("Adding File...");

//        DBObject metaData = new BasicDBObject();
//        metaData.put("flow", "video");
//        metaData.put("title", title);


        ObjectId id = gridFsTemplate.store(
                file.getInputStream(), file.getOriginalFilename(), file.getContentType());
        return id.toString();

    }

    public RequestFile getFile(String id) throws IllegalStateException, IOException {

        GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));

        if (gridFsFile == null) throw new IOException("File with id: " + id + " not found!");

        InputStreamResource resource = new InputStreamResource(operations.getResource(gridFsFile).getInputStream());
        RequestFile file = new RequestFile();
        file.setFilename(gridFsFile.getFilename());
        file.setResource(resource);

        return file;


    }

}
