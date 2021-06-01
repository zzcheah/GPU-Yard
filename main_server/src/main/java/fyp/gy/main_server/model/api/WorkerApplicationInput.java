package fyp.gy.main_server.model.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class WorkerApplicationInput {

    private String name;
    private String email;
    private int maxTasks;
    private String ipAddress;


}
