package com.backend.ResCall.jwt;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RefreshTokenStore {
    private final Map<String, String> tokenStore = new ConcurrentHashMap<>();

    public void save(String email, String refreshToken) {
        tokenStore.put(email, refreshToken);
    }

    public boolean isValid(String email, String refreshToken) {
        return refreshToken.equals(tokenStore.get(email));
    }

    public void remove(String email) {
        tokenStore.remove(email);
    }
}
