package com.skillify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    @Autowired UserRepo users;
    final BCryptPasswordEncoder enc = new BCryptPasswordEncoder();

    @PostMapping("/signup")
    public Object signup(@RequestBody Map<String, Object> b) {
        String email = s(b, "email").toLowerCase(), pw = s(b, "password");
        if (s(b, "name").isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter your name");
        if (email.isBlank() || pw.length() < 6) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter an email and a password of at least 6 characters");
        if (users.findByEmail(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered. Log in instead.");
        User u = new User();
        u.email = email; u.passwordHash = enc.encode(pw); u.apply(b);
        return session(u);
    }

    @PostMapping("/login")
    public Object login(@RequestBody Map<String, Object> b) {
        User u = users.findByEmail(s(b, "email").toLowerCase()).orElse(null);
        if (u == null || !enc.matches(s(b, "password"), u.passwordHash)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password");
        return session(u);
    }

    Object session(User u) {
        u.token = UUID.randomUUID().toString();
        users.save(u);
        return Map.of("token", u.token, "name", u.name);
    }
    static String s(Map<String, Object> b, String k) { Object v = b.get(k); return v == null ? "" : v.toString().trim(); }
}
