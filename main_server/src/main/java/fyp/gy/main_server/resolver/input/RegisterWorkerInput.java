package fyp.gy.main_server.resolver.input;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class RegisterWorkerInput {

    private String name;
    private int maxTasks;
    private String ipAddress;

}
