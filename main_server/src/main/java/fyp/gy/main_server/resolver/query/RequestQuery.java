package fyp.gy.main_server.resolver.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fyp.gy.main_server.repository.RequestRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

@Component
@CrossOrigin(origins = "http://localhost:3000")
public class RequestQuery implements GraphQLQueryResolver {

    private final RequestRepository requestRepo;

    public RequestQuery(RequestRepository requestRepo) {
        this.requestRepo = requestRepo;
    }

    public int countRequests() {
        return Math.toIntExact(requestRepo.count());
    }



}
