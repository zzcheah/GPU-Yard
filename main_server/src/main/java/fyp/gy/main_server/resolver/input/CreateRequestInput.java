package fyp.gy.main_server.resolver.input;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class CreateRequestInput {

    private String name;
    private String image;
    private String param;
    private List<String> inputFiles;

}
