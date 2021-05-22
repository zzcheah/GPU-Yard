package fyp.gy.main_server.resolver.input;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterWorkerInput {

    private String name;
    private int maxTasks;
    private String ipAddress;

}
