package fyp.gy.main_server.repository;

import fyp.gy.main_server.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User,String> {
}
