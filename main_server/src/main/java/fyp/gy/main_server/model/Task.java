package fyp.gy.main_server.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Document(collection = "Tasks")
public class Task implements Comparable<Task> {

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

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    @Override
    public int compareTo(Task o) {
        try {
            Date d1 = formatter.parse(this.getCreatedAt());
            Date d2 = formatter.parse(o.getCreatedAt());
            return d1.compareTo(d2);
        } catch (ParseException e) {
            return 0;
        }

    }
}
