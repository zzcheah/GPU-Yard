package fyp.gy.main_server.model;

import fyp.gy.common.constant.GyConstant;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = GyConstant.USERS_COLLECTION)
@Getter @Setter
@Builder
public class User {

    @Id
    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String status;
    private String password;

}
