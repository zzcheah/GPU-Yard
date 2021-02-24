package fyp.gy.main_server.model;


import java.util.ArrayList;
import java.util.List;

public class RequestDetail {
    private String status;
    private String userID;
    private String requestID;
    private String createdAt;
    private String encodedParam;
    private List<String> inputFiles;
    private List<String> outputFiles;

    public String getStatus() {
        return status;
    }
    public String getUserID() {
        return userID;
    }
    public String getRequestID() {
        return requestID;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public String getEncodedParam() {
        return encodedParam;
    }
    public List<String> getInputFiles() {
        return inputFiles!=null? inputFiles: new ArrayList<>();
    }
    public List<String> getOutputFiles() {
        return outputFiles!=null? outputFiles: new ArrayList<>();
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }
    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
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
