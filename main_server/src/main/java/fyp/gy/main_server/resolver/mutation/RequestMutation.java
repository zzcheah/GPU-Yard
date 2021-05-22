package fyp.gy.main_server.resolver.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fyp.gy.common.model.Request;
import fyp.gy.main_server.repository.RequestRepository;
import fyp.gy.main_server.service.RequestService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

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

    public Request createRequest(String payload) throws Exception {

        Map<String, Object> map = decodeJson(payload);
        return requestService.addRequest(map);

    }

    private Map<String, Object> decodeJson(String encodedPayload) throws JsonProcessingException {

        byte[] decodedBytes = Base64.getDecoder().decode(encodedPayload);
        String decodedString = new String(decodedBytes);

        return new ObjectMapper().readValue(decodedString, new TypeReference<Map<String, Object>>() {
        });
    }
}
