package fyp.gy.main_server.repository;

import fyp.gy.common.model.DockerImage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DockerImageRepository extends MongoRepository<DockerImage,String> {

    Optional<DockerImage> findByName(String name);
}
