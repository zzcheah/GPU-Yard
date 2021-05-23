package fyp.gy.main_server.model.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter @Setter
@AllArgsConstructor
public class RegistrationRequest {

    private String name;
    private String email;
    private String password;


}
