package fyp.gy.common.model;


import fyp.gy.common.constant.GyConstant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
@Getter @Setter
@Document(collection = GyConstant.REQUESTS_COLLECTION)
public class Request {

    @Id
    private String id;
    private String status;
    private String userID;
    private String createdAt;
    private String title;
    private String image;
    private String encodedParam;
    private String assignedTo;
    private String remark;
    private List<String> inputFiles = new ArrayList<>();
    private List<String> outputFiles = new ArrayList<>();

}
