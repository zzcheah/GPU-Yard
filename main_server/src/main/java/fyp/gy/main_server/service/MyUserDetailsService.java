package fyp.gy.main_server.service;

import fyp.gy.main_server.model.auth.MyUserDetails;
import fyp.gy.main_server.model.User;
import fyp.gy.main_server.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        System.out.println(s);
        Optional<User> user = userRepository.findByEmail(s);
        user.orElseThrow(()-> new UsernameNotFoundException("User not found"));
        return user.map(MyUserDetails::new).get();
    }
}
