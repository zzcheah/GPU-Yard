package fyp.gy.task_worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class TaskWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskWorkerApplication.class, args);
	}

}
