package fyp.gy.main_server.resolver.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fyp.gy.main_server.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserMutation implements GraphQLMutationResolver {

    Logger logger = LoggerFactory.getLogger(UserMutation.class);

    private final UserRepository repo;
    private final MongoTemplate template;

    public UserMutation(UserRepository repo, MongoTemplate template, PasswordEncoder encoder) {
        this.repo = repo;
        this.template = template;
    }

}
