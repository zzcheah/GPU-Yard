package fyp.gy.common.model;


import fyp.gy.common.constant.GyConstant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter @Setter
@Builder
@Document(collection = GyConstant.WORKER_APPLICATION_COLLECTION)
public class WorkerApplication {

    @Id
    private String id;
    private String name;
    private String email;
    private int maxTasks;
    private String ipAddress;
    private String createdAt;

}
