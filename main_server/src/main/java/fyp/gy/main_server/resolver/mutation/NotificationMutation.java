package fyp.gy.main_server.resolver.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fyp.gy.common.model.Notification;
import fyp.gy.main_server.model.Worker;
import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.model.auth.UserProfile;
import fyp.gy.main_server.repository.NotificationRepository;
import fyp.gy.main_server.resolver.input.RegisterWorkerInput;
import graphql.GraphQLException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationMutation implements GraphQLMutationResolver {

    private final NotificationRepository repo;
    private final MongoTemplate template;

    public NotificationMutation(NotificationRepository repo, MongoTemplate template) {
        this.repo = repo;
        this.template = template;
    }

    public Notification setReadNotification(String id){

        Notification notification = repo.findById(id).orElse(null);
        if(notification==null) throw new GraphQLException("Notification not found!");

        UserProfile profile = userDetails().getProfile();
        if(!notification.getUser().equals(profile.getId())) throw new GraphQLException("Forbidden Access");

        notification.setRead(!notification.isRead());
        template.save(notification);

        return notification;

    }


    private MyUserDetails userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (MyUserDetails) authentication.getPrincipal();
    }


}
