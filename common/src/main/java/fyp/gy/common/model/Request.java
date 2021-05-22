package fyp.gy.common.model;


import fyp.gy.common.constant.GyConstant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = GyConstant.REQUESTS_COLLECTION)
public class Request {

    @Id
    private String id;
    private String status;
    private String userID;
    private String createdAt;
    private String title;
    private String image;
    private String encodedParam;
    private String assignedTo;
    private List<String> inputFiles;
    private List<String> outputFiles;

    public String getId() {
        return id;
    }
    public String getStatus() {
        return status;
    }
    public String getUserID() {
        return userID;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getTitle() {
        return title;
    }
    public String getImage() {
        return image;
    }
    public String getEncodedParam() {
        return encodedParam;
    }
    public String getAssignedTo() {
        return assignedTo;
    }
    public List<String> getInputFiles() {
        return inputFiles!=null? inputFiles: new ArrayList<>();
    }
    public List<String> getOutputFiles() {
        return outputFiles!=null? outputFiles: new ArrayList<>();
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
    public void setEncodedParam(String encodedParam) {
        this.encodedParam = encodedParam;
    }
    public void setInputFiles(List<String> inputFiles) {
        this.inputFiles = inputFiles;
    }
    public void setOutputFiles(List<String> outputFiles) {
        this.outputFiles = outputFiles;
    }
}
