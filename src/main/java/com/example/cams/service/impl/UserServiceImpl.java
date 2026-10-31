package com.example.cams.service.impl;

import com.example.cams.entity.User;
import com.example.cams.repository.UserRepository;
import com.example.cams.service.UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found."));
    }
}
