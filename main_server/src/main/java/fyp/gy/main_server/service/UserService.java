package fyp.gy.main_server.service;

import fyp.gy.main_server.model.User;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    final MongoTemplate template;

    public UserService(MongoTemplate template) {
        this.template = template;

        User user = new User();
        System.out.println("asdasdasd");
        user.setName("Cheah Zhong Zhi");
        template.save(user, "Users");

    }


}
