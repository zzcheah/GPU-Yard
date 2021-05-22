package fyp.gy.common.model;

import fyp.gy.common.constant.GyConstant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = GyConstant.IMAGE_TAG_COLLECTION)
@Builder
@Setter @Getter
public class ImageTag {

    @Id
    private String id;
    private String image;
    private String tag;
    private String description;
}
