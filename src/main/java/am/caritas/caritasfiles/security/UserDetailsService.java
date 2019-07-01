package am.caritas.caritasfiles.security;


import am.caritas.caritasfiles.model.User;
import am.caritas.caritasfiles.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    UserRepository userRepository;



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User byEmail = userRepository.findByEmail(email);

        if(byEmail==null){
            throw new UsernameNotFoundException("User not found");
        }
        return new CurrentUser(byEmail);
    }
}
