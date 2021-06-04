package fyp.gy.main_server.resolver.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fyp.gy.common.model.ImageRequest;
import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.model.auth.UserProfile;
import fyp.gy.main_server.repository.ImageRequestRepository;
import fyp.gy.main_server.resolver.input.AddImageRequestInput;
import fyp.gy.main_server.resolver.input.AddNewImageInput;
import fyp.gy.main_server.util.DateTimeUtil;
import graphql.GraphQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ImageRequestMutation implements GraphQLMutationResolver {


    private final MongoTemplate template;
    private final ImageRequestRepository repo;
    private final DockerImageMutation dockerImageMutation;

    public ImageRequestMutation(MongoTemplate template, ImageRequestRepository repo, DockerImageMutation dockerImageMutation) {
        this.template = template;
        this.repo = repo;
        this.dockerImageMutation = dockerImageMutation;
    }


    public ImageRequest addImageRequest(AddImageRequestInput input) {
        ImageRequest ir = ImageRequest.builder()
                .image(input.getImage())
                .tag(input.getTag())
                .remark(input.getRemark())
                .createdAt(DateTimeUtil.getCurrentTime())
                .build();

        ir = template.save(ir);

        return ir;
    }

    public ImageRequest approveImage(String id) {

        UserProfile profile = userDetails().getProfile();
        if (!profile.getRole().equals("ADMIN")) throw new GraphQLException("Forbidden Access");

        ImageRequest ir = repo.findById(id).orElse(null);
        if (ir == null) throw new GraphQLException("ImageRequest not found!");


        AddNewImageInput input = AddNewImageInput.builder()
                .image(ir.getImage())
                .tag(ir.getTag())
                .description("Unavailable")
                .build();
        try {
            dockerImageMutation.addNewImage(input);
        } catch (Exception e) {
            throw new GraphQLException(e.getMessage());
        }
        repo.deleteById(id);

        return ir;

    }

    private MyUserDetails userDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (MyUserDetails) authentication.getPrincipal();
    }

}
