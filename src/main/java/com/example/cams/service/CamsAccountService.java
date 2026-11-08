package com.example.cams.service;

import com.example.cams.entity.CamsUser;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.repository.CamsUserRepository;
import com.example.cams.util.exception.CamsApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.example.cams.util.CamsValidation.require;
import static com.example.cams.util.CamsValidation.text;

/** Registration and login credential checks — mirrors app.py's /api/register and /api/login. */
@Service
public class CamsAccountService {

    private final CamsUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CamsAccountService(CamsUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long register(Map<String, Object> data) {
        String name = text(data.get("name"), "Họ tên", 100);
        String email = text(data.get("email"), "Email", 150).toLowerCase();
        require(email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"), "Email không hợp lệ.");
        String password = text(data.get("password"), "Mật khẩu", 128, 10);
        String phone = text(data.getOrDefault("phone", ""), "Số điện thoại", 20, 0);
        require(phone.isEmpty() || phone.matches("[+0-9 ()-]{8,20}"), "Số điện thoại không hợp lệ.");

        CamsUser user = new CamsUser();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(CamsRole.patient);
        user.setPhone(phone);
        try {
            return userRepository.save(user).getId();
        } catch (DataIntegrityViolationException e) {
            throw new CamsApiException(409, "Email đã được sử dụng.", "email_exists");
        }
    }

}
