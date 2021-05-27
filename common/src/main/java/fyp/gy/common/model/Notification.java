package fyp.gy.common.model;


import fyp.gy.common.constant.GyConstant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter
@Builder
@Document(collection = GyConstant.NOTIFICATIONS_COLLECTION)
public class Notification {

    @Id
    private String id;
    private boolean isRead = false;
    private String user;
    private String content;
    private String severity = "info";
    private String createdAt;
    private String link;


}
