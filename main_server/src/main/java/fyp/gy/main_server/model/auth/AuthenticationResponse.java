package fyp.gy.main_server.model.auth;

public class AuthenticationResponse {

    private final String jwt;
    private final UserProfile profile;


    public AuthenticationResponse(String jwt,UserProfile profile) {
        this.jwt = jwt;
        this.profile = profile;
    }

    public String getJwt() {
        return jwt;
    }

    public UserProfile getProfile() {
        return profile;
    }
}
