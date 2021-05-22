package fyp.gy.common.model;

import fyp.gy.common.constant.GyConstant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = GyConstant.DOCKER_IMAGE_COLLECTION)
@Builder
@Setter @Getter
public class DockerImage {

    @Id
    private String id;
    private String name;
    private List<String> tags;
    private int usageCount;
    private String description;
}
