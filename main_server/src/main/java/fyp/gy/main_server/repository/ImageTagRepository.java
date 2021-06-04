package fyp.gy.main_server.repository;

import fyp.gy.common.model.ImageTag;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ImageTagRepository extends MongoRepository<ImageTag,String> {

    List<ImageTag> findByImage(String image);

    ImageTag findByImageAndTag(String image, String tag);
}
