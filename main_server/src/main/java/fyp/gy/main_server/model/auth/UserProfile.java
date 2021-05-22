package fyp.gy.main_server.model.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UserProfile {

    private String id;
    private String email;
    private String name;
    private String phoneNumber;
    private String role;
}
