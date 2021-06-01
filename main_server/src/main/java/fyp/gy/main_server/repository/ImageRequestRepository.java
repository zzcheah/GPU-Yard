package fyp.gy.main_server.repository;

import fyp.gy.common.model.ImageRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ImageRequestRepository extends MongoRepository<ImageRequest,String> {

}
