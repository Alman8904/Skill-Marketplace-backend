package com.Skill.Marketplace.SM.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, String> health() {
        Map<String, String> response = new LinkedHashMap<>();
        response.put("app", "Skill Marketplace API");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("docs", "/swagger-ui.html");
        return response;
    }
}