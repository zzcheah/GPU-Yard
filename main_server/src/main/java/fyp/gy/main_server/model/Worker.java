package fyp.gy.main_server.model;

import fyp.gy.common.constant.GyConstant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = GyConstant.WORKERS_COLLECTION)
public class Worker {

    @Id
    private String id;
    private String name;
    private int maxTasks;
    private String ipAddress;
    private String status;
    private Date lastActive;
    private List<String> runningTasks = new ArrayList<>();

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public int getMaxTasks() {
        return maxTasks;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public String getStatus() {
        return status;
    }
    public Date getLastActive() {
        return lastActive;
    }
    public List<String> getRunningTasks() {
        return runningTasks;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setMaxTasks(int maxTasks) {
        this.maxTasks = maxTasks;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }
    public void setRunningTasks(List<String> runningTasks) {
        this.runningTasks = runningTasks;
    }
}
