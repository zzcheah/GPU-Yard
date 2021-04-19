package fyp.gy.main_server.repository;

import fyp.gy.common.model.Request;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RequestRepository extends MongoRepository<Request,String> {

}
