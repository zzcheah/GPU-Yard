package fyp.gy.main_server.controller;


import fyp.gy.main_server.model.auth.AuthenticationRequest;
import fyp.gy.main_server.model.auth.AuthenticationResponse;
import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.service.MyUserDetailsService;
import fyp.gy.main_server.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class ReactController {

    private final AuthenticationManager authenticationManager;
    private final MyUserDetailsService myUserDetailsService;
    private final JwtUtil jwtUtil;

    public ReactController(
            AuthenticationManager authenticationManager,
            MyUserDetailsService myUserDetailsService,
            JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.myUserDetailsService = myUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @RequestMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody AuthenticationRequest req) throws Exception {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(),req.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new Exception("Incorrect email or password",e);
        }

        final UserDetails userDetails = myUserDetailsService.loadUserByUsername(req.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponse(jwt,((MyUserDetails)userDetails).getProfile()));



    }
}