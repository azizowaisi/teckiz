package com.teckiz.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Password encoder that supports both Symfony's $2y$ format and Spring Boot's $2a$ format.
 * Symfony uses $2y$ prefix for bcrypt, while Spring Boot uses $2a$.
 * This encoder converts $2y$ to $2a$ for verification and always encodes new passwords with $2a$.
 */
public class SymfonyCompatiblePasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcryptEncoder = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence rawPassword) {
        // Always encode new passwords with $2a$ format
        return bcryptEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.length() == 0) {
            return false;
        }

        // Convert Symfony's $2y$ format to $2a$ format for verification
        // Both formats are compatible, just different prefixes
        String normalizedPassword = encodedPassword;
        if (encodedPassword.startsWith("$2y$")) {
            normalizedPassword = "$2a$" + encodedPassword.substring(4);
        }

        return bcryptEncoder.matches(rawPassword, normalizedPassword);
    }
}

