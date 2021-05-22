package fyp.gy.main_server.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fyp.gy.common.model.DockerImage;
import fyp.gy.common.model.ImageTag;
import fyp.gy.main_server.repository.ImageTagRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@Component
@CrossOrigin(origins = "http://localhost:3000")
public class DockerImageResolver implements GraphQLResolver<DockerImage> {

    private final ImageTagRepository tagRepo;

    public DockerImageResolver(ImageTagRepository tagRepo) {
        this.tagRepo = tagRepo;
    }

    public List<ImageTag> getTags(DockerImage image) {
        return tagRepo.findByImage(image.getName());
    }

}
