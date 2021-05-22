package fyp.gy.main_server.repository;

import fyp.gy.common.model.Request;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RequestRepository extends MongoRepository<Request,String> {

    List<Request> findByUserID(String userID);


}
