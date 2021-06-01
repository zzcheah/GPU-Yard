package fyp.gy.main_server.resolver.mutation;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fyp.gy.common.model.ImageRequest;
import fyp.gy.main_server.resolver.input.AddImageRequestInput;
import fyp.gy.main_server.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ImageRequestMutation implements GraphQLMutationResolver {


    private final MongoTemplate template;

    public ImageRequestMutation(MongoTemplate template) {
        this.template = template;
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

}
