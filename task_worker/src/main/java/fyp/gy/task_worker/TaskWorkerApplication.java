package fyp.gy.task_worker;

import fyp.gy.common.constant.GyConstant;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;
import java.util.prefs.Preferences;

@SpringBootApplication(exclude = {
		MongoAutoConfiguration.class,
		MongoDataAutoConfiguration.class
})
public class TaskWorkerApplication {

	public static void main(String[] args) throws IOException {
		Preferences pref = Preferences.userRoot().node(GyConstant.PREFERENCE_NODE);
		boolean verified =pref.getBoolean("Verified",false);
		if(verified){
			SpringApplication.run(TaskWorkerApplication.class, args);
		} else {

			Scanner sc= new Scanner(System.in);
			int option = 0;
			while (option<1||option>2) {
				System.out.println("Select from the options below:");
				System.out.println("1. Register Your Worker ");
				System.out.println("2. Enter Worker ID (after approval)");
				option = sc.nextInt();
			}
			if(option==1) {
				String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath()+"application.properties";
				Properties appProps = new Properties();
				appProps.load(new FileInputStream(rootPath));
				String url = String.format("http://%s:3000/newWorker",appProps.getProperty("mainserver.hostname"));
				java.awt.Desktop.getDesktop().browse(URI.create(url));
			} else {
				System.out.println("Enter Worker Id:");
				String id = sc.next();
				pref.put("id",id);
				SpringApplication.run(TaskWorkerApplication.class, args);
			}

		}


	}

}
