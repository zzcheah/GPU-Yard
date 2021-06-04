package fyp.gy.main_server.resolver.input;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class AddNewImageInput {

    private String image;
    private String tag;
    private String description;

}
