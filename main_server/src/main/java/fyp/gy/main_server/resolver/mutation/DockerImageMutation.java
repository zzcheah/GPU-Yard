package fyp.gy.main_server.resolver.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fyp.gy.common.model.DockerImage;
import fyp.gy.common.model.ImageTag;
import fyp.gy.main_server.repository.DockerImageRepository;
import fyp.gy.main_server.repository.ImageTagRepository;
import fyp.gy.main_server.resolver.input.AddNewImageInput;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;

@Component
@Slf4j
public class DockerImageMutation implements GraphQLMutationResolver {

    private final DockerImageRepository imageRepo;
    private final ImageTagRepository tagRepo;
    private final MongoTemplate template;

    public DockerImageMutation(DockerImageRepository imageRepo, ImageTagRepository tagRepo, MongoTemplate template) {
        this.imageRepo = imageRepo;
        this.tagRepo = tagRepo;
        this.template = template;
    }


    public ImageTag addNewImage(AddNewImageInput input) throws Exception {

        DockerImage image = imageRepo.findByName(input.getImage()).orElse(null);
        if (image == null) {
            image = DockerImage.builder()
                    .name(input.getImage())
                    .tags(new ArrayList<>())
                    .description("General description for image " + input.getImage())
                    .build();
            template.save(image);
        }

        ImageTag exist = tagRepo.findByImageAndTag(input.getImage(),input.getTag());
        if(exist!=null) throw new Exception("Image already exists");

        ImageTag tag = ImageTag.builder()
                .image(input.getImage())
                .description(input.getDescription())
                .tag(input.getTag())
                .build();

        tag = tagRepo.insert(tag);
        image.getTags().add(tag.getId());
        imageRepo.save(image);

        log.info("registered a new docker image");

        return tag;

    }

}
