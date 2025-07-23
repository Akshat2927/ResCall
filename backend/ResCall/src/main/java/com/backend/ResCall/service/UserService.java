package com.backend.ResCall.service;

import com.backend.ResCall.entity.User;
import com.backend.ResCall.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;

    public User registerUser(User user) {
        if(user.getEmail()==null || user.getPassword()==null || user.getName()==null){
            throw new NullPointerException("Enter the required fields");
        }else {
            String password = passwordEncoder.encode(user.getPassword());
            user.setPassword(password);
            return userRepository.save(user);
        }
    }

    public User loginUser(User user) {
        User user1 = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        String hashPassword = user1.getPassword();
        String name = user1.getName();
        boolean password = passwordEncoder.matches(user.getPassword(), hashPassword);
        if (!password){
            throw new BadCredentialsException("Invalid password");
        }
        return user1;
    }
}
