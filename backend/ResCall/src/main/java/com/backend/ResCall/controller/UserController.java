package com.backend.ResCall.controller;


import com.backend.ResCall.jwt.AuthRequest;
import com.backend.ResCall.jwt.AuthResponse;
import com.backend.ResCall.entity.User;
import com.backend.ResCall.service.JwtService;
import com.backend.ResCall.jwt.RefreshTokenStore;
import com.backend.ResCall.repository.UserRepository;
import com.backend.ResCall.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController

@CrossOrigin(origins = "https://goel-ansh.github.io/ResCall/")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenStore refreshTokenStore;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home(){
        return "Application is live and running ";
    }
    
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }


    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String accessToken = jwtService.generateAccessToken(request.getEmail());
        String refreshToken = jwtService.generateRefreshToken(request.getEmail());

        refreshTokenStore.save(request.getEmail(), refreshToken);

        return new AuthResponse(accessToken, refreshToken);
    }


    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestBody String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);

        if (!jwtService.isTokenValid(refreshToken, email) || !refreshTokenStore.isValid(email, refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(email);
        return new AuthResponse(newAccessToken, refreshToken);
    }


    @PostMapping("/logout")
    public String logout(@RequestBody String refreshToken) {
        String email = jwtService.extractUsername(refreshToken);
        refreshTokenStore.remove(email);
        return "User logged out successfully.";
    }
}
