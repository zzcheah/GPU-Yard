package fyp.gy.main_server.resolver.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fyp.gy.common.model.Notification;
import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.repository.NotificationRepository;
import fyp.gy.main_server.util.DateTimeUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NotificationQuery implements GraphQLQueryResolver {

    private final NotificationRepository repo;

    public NotificationQuery(NotificationRepository repo) {
        this.repo = repo;
    }


    public List<Notification> getNotificationList() {
        List<Notification> list = repo.findByUser(userDetails().getProfile().getId());
        for (Notification notification : list) {
            if (notification.getCreatedAt() == null) {
                notification.setCreatedAt(DateTimeUtil.getCurrentTime());
                repo.save(notification);
            }
        }
        return list;
    }

    private MyUserDetails userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (MyUserDetails) authentication.getPrincipal();
    }

}
