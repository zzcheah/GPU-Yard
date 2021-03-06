package fyp.gy.main_server.resolver.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fyp.gy.common.model.Request;
import fyp.gy.main_server.model.Worker;
import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.model.auth.UserProfile;
import fyp.gy.main_server.repository.RequestRepository;
import fyp.gy.main_server.resolver.input.CreateRequestInput;
import fyp.gy.main_server.resolver.input.RegisterWorkerInput;
import fyp.gy.main_server.service.RequestService;
import graphql.GraphQLException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Base64;
import java.util.Map;

@Component
public class RequestMutation implements GraphQLMutationResolver {

    private final RequestRepository requestRepo;
    private final RequestService requestService;

    public RequestMutation(RequestRepository requestRepo, RequestService requestService) {
        this.requestRepo = requestRepo;
        this.requestService = requestService;
    }


    public Request createRequest(CreateRequestInput input) {

        UserProfile profile = userDetails().getProfile();
        if (!profile.getRole().equals("USER")) throw new GraphQLException("Forbidden Access");
        if (!profile.getRole().equals("Active")) throw new GraphQLException("User not allowed to make request");
        return requestService.createRequest(input, profile.getId());

    }

    private MyUserDetails userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (MyUserDetails) authentication.getPrincipal();
    }

//    private Map<String, Object> decodeJson(String encodedPayload) throws JsonProcessingException {
//
//        byte[] decodedBytes = Base64.getDecoder().decode(encodedPayload);
//        String decodedString = new String(decodedBytes);
//
//        return new ObjectMapper().readValue(decodedString, new TypeReference<Map<String, Object>>() {
//        });
//    }
}
