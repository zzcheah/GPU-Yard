package fyp.gy.main_server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Tasks")
public class Task {

    @Id
    private String id;
    private String title;
    private String requestID;
    private String createdAt;

    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getRequestID() {
        return requestID;
    }
    public String getCreatedAt() {
        return createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
