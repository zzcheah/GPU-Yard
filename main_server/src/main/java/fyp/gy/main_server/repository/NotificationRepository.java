package fyp.gy.main_server.repository;

import fyp.gy.common.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification,String> {

    List<Notification> findByUser(String userID);
}
