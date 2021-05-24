package fyp.gy.main_server.model;

import fyp.gy.common.constant.GyConstant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter @Setter
@Document(collection = GyConstant.TASKS_COLLECTION)
public class Task implements Comparable<Task> {

    @Id
    private String id;
    private String requestID;
    private String createdAt;
    private String assignedTo;
    private Date startTime;

    private static final SimpleDateFormat formatter = new SimpleDateFormat(GyConstant.DATE_TIME_FORMAT);

    @Override
    public int compareTo(Task o) {
        try {

            // prioritize not assigned,
            Date d1 = formatter.parse(this.getCreatedAt());
            Date d2 = formatter.parse(o.getCreatedAt());
            return d1.compareTo(d2);
        } catch (ParseException e) {
            return 0;
        }

    }
}
