package fyp.gy.main_server.resolver.input;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AddImageRequestInput {

    private String image;
    private String tag;
    private String remark;

}
