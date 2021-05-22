package fyp.gy.main_server.repository;

import fyp.gy.main_server.model.Worker;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WorkerRepository extends MongoRepository<Worker,String> {

    Optional<Worker> findByName(String name);
}
