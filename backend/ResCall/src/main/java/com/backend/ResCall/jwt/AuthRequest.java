package com.backend.ResCall.jwt;


import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
