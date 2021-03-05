package fyp.gy.main_server.service;

import fyp.gy.common.constant.GyConstant;
import fyp.gy.main_server.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    Logger logger = LoggerFactory.getLogger(User.class);

    final MongoTemplate template;

    public UserService(MongoTemplate template) {
        this.template = template;
        logger.info("Starting User Service...");
    }


    public User addNewUser(String name) {
        logger.info("Adding new user...");

        try {
            User user = new User();
            user.setName(name);
            return template.save(user, GyConstant.USERS_COLLECTION);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    public User findUser(String id) {
        logger.info("Finding for user " + id + " ...");

        try {
            return template.findById(id, User.class, GyConstant.USERS_COLLECTION);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }

    }

}
