package fyp.gy.main_server.resolver.query;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fyp.gy.common.model.DockerImage;
import fyp.gy.main_server.repository.DockerImageRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImageQuery implements GraphQLQueryResolver {

    private final DockerImageRepository imageRepo;

    public ImageQuery(DockerImageRepository imageRepo) {
        this.imageRepo = imageRepo;
    }

    public List<DockerImage> getDockerImages() {
        return imageRepo.findAll();
    }

}
