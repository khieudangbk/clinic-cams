package com.example.cams.service;

import com.example.cams.dto.request.account.LoginRequest;
import com.example.cams.dto.request.account.RegisterRequest;
import com.example.cams.entity.CamsUser;
import com.example.cams.entity.enums.CamsRole;
import com.example.cams.repository.CamsUserRepository;
import com.example.cams.security.CamsLoginRateLimiter;
import com.example.cams.util.exception.CamsApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.cams.util.CamsValidation.require;
import static com.example.cams.util.CamsValidation.text;

/** Registration and login credential checks — mirrors app.py's /api/register and /api/login. */
@Service
public class CamsAccountService {

    private final CamsUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CamsLoginRateLimiter rateLimiter;

    public CamsAccountService(CamsUserRepository userRepository, PasswordEncoder passwordEncoder,
            CamsLoginRateLimiter rateLimiter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rateLimiter = rateLimiter;
    }

    @Transactional
    public Long register(RegisterRequest data) {
        String name = text(data.name(), "Họ tên", 100);
        String email = text(data.email(), "Email", 150).toLowerCase();
        require(email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"), "Email không hợp lệ.");
        String password = text(data.password(), "Mật khẩu", 128, 10);
        String phone = text(data.phone() == null ? "" : data.phone(), "Số điện thoại", 20, 0);
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

    /** Verifies credentials and the account's lockout window; returns the authenticated user. */
    @Transactional
    public CamsUser login(LoginRequest data) {
        String email = text(data.email(), "Email", 150).toLowerCase();
        String password = text(data.password(), "Mật khẩu", 128);

        rateLimiter.requireNotLocked(email);
        CamsUser user = userRepository.findByEmailAndActiveTrue(email).orElse(null);
        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(password, user.getPasswordHash())) {
            rateLimiter.recordFailure(email);
            throw new CamsApiException(401, "Email hoặc mật khẩu không đúng.", "invalid_credentials");
        }
        rateLimiter.clear(email);
        return user;
    }
}
