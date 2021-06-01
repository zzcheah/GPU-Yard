package fyp.gy.common.model;


import fyp.gy.common.constant.GyConstant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter
@Builder
@Document(collection = GyConstant.IMAGE_REQUEST_COLLECTION)
public class ImageRequest {

    @Id
    private String id;
    private String image;
    private String tag;
    private String remark;
    private String createdAt;

}
