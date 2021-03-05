package fyp.gy.main_server.model;

import fyp.gy.common.constant.GyConstant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = GyConstant.USERS_COLLECTION)
public class User {

    @Id
    private String id;
    private String name;

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
}
