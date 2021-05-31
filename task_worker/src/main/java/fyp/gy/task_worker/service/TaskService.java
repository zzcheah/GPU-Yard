package fyp.gy.task_worker.service;

import fyp.gy.common.constant.GyConstant;
import fyp.gy.common.model.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

@Service
public class TaskService {


    Logger logger = LoggerFactory.getLogger(TaskService.class);

    // Autowired
    private final DockerService docker;
    private final RestTemplate restTemplate;


    // internal use
    private final String pollURL;
    private Timer timer;
    private long poolingInterval;

    @Autowired
    public TaskService(DockerService docker,
                       @Value("${mainserver.hostname}") String hn,
                       @Value("${mainserver.port}") String port
    ) throws Exception {

        this.docker = docker;
        this.restTemplate = new RestTemplate();

        String msURL = String.format("http://%s:%s", hn, port);

        Preferences pref = Preferences.userRoot().node(GyConstant.PREFERENCE_NODE);
        String id = pref.get("id", null);
        if(id==null) {
            pref.putBoolean("Verified", true);
            throw new Exception("Missing Machine ID");
        }

        Boolean valid = restTemplate.getForObject(msURL+"/verify?id="+id,Boolean.class);
        if(valid!=null && valid) {
            pref.putBoolean("Verified", true);
        } else {
            pref.putBoolean("Verified", false);
            throw new Exception("Invalid Machine ID");
        }

        this.pollURL = msURL + "/request/poll?worker=" + id;
        resetTimer();

    }

    private void pollAndRun() {

        logger.info("Polling job from main server");
        try {
            Request request = restTemplate.getForObject(pollURL,Request.class);
            if(request!=null) {
                resetTimer();
                docker.process(request);
            }
            else increasePoolingInterval();
        } catch (RestClientException e) {
//            e.printStackTrace();
            increasePoolingInterval();
            logger.error(e.getMessage());
        }
    }

    private void resetTimer() {
        if (timer!=null) timer.cancel();
        timer = new Timer();
        poolingInterval = TimeUnit.SECONDS.toMillis(10);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pollAndRun();
            }
        }, poolingInterval, poolingInterval);
    }

    private void increasePoolingInterval() {
        timer.cancel();
        timer = new Timer();
        poolingInterval = (long) Math.min(TimeUnit.MINUTES.toMillis(5),poolingInterval*1.5);
//        System.out.println(TimeUnit.MILLISECONDS.toSeconds(poolingInterval));

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                pollAndRun();
            }
        }, poolingInterval, poolingInterval);
    }


}

