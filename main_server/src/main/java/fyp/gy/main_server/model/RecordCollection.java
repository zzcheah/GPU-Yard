package fyp.gy.main_server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "RecordCollection")
public class RecordCollection {

    @Id
    private String id;
    private String title;
    private List<RequestDetail> requests;

    public String getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public List<RequestDetail> getRequests() {
        return requests;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setRequests(List<RequestDetail> requests) {
        this.requests = requests;
    }
}
