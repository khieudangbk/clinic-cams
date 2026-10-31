package com.example.cams.service;

import com.example.cams.dto.request.auth.LoginRequest;
import com.example.cams.dto.request.auth.RegisterRequest;
import com.example.cams.dto.response.auth.AuthResponse;
import com.example.cams.dto.response.auth.LoginResult;

public interface AuthService {

    AuthResponse.UserInfo register(RegisterRequest request);

    LoginResult login(LoginRequest request);

    LoginResult refresh(String request);

    void logout(String email);

    AuthResponse.UserInfo getCurrentUserProfile(String email);
}
