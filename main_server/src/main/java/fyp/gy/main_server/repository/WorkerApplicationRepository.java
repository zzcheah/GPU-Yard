package fyp.gy.main_server.repository;

import fyp.gy.common.model.Notification;
import fyp.gy.common.model.WorkerApplication;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface WorkerApplicationRepository extends MongoRepository<WorkerApplication,String> {

}
