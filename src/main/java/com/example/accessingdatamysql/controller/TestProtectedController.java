package com.example.accessingdatamysql.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestProtectedController {

    @GetMapping("/protected")
    public Map<String, Object> protectedEndpoint(Authentication authentication) {
        return Map.of(
                "message", "You reached a protected endpoint",
                "authenticated", true,
                "principal", authentication.getName()
        );
    }
}