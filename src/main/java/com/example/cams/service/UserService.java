package com.example.cams.service;

import com.example.cams.entity.User;

public interface UserService {
    User getUserByEmail(String email);
}
