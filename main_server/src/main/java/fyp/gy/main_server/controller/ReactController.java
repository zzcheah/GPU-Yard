package fyp.gy.main_server.controller;


import fyp.gy.main_server.model.User;
import fyp.gy.main_server.model.auth.AuthenticationRequest;
import fyp.gy.main_server.model.auth.AuthenticationResponse;
import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.model.auth.RegistrationRequest;
import fyp.gy.main_server.repository.UserRepository;
import fyp.gy.main_server.service.MyUserDetailsService;
import fyp.gy.main_server.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReactController {

    private final AuthenticationManager authenticationManager;
    private final MyUserDetailsService myUserDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public ReactController(
            AuthenticationManager authenticationManager,
            MyUserDetailsService myUserDetailsService,
            UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.myUserDetailsService = myUserDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody AuthenticationRequest req) throws Exception {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new Exception("Incorrect email or password", e);
        }

        final UserDetails userDetails = myUserDetailsService.loadUserByUsername(req.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponse(jwt, ((MyUserDetails) userDetails).getProfile()));

    }

    @PostMapping("/signup")
    public ResponseEntity<?> createAuthenticationToken(
            @RequestBody RegistrationRequest input) {

        User user = User.builder()
                .name(input.getName())
                .email(input.getEmail())
                .password(passwordEncoder.encode(input.getPassword()))
                .role("USER")
                .status("Inactive")
                .build();

        userRepository.save(user);

        return ResponseEntity.ok("Success Registered User");

    }
}
