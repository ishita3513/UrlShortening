package com.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class HomeController {

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping("/")
    public ResponseEntity<String> home() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:static/index.html");
        String content = Files.readString(Paths.get(resource.getURI()), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header("Content-Type", "text/html;charset=UTF-8")
                .body(content);
    }
}
