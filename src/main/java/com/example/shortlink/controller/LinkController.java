package com.example.shortlink.controller;

import com.example.shortlink.dto.CreateLinkRequest;
import com.example.shortlink.service.LinkService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Validated
public class LinkController {

    private final LinkService linkService;

    @PostMapping("/add")
    public String addLink(@Valid @RequestBody CreateLinkRequest request) {
        String domen = "http://localhost:8080";
        String code = linkService.addLink(request);
        return "Ваш новый адрес: " + domen + "/" + code;
    }

    @GetMapping("/{code}")
    public void redirect(@PathVariable String code, HttpServletResponse response) throws IOException {
        String uri = linkService.getFullUrl(code);
        response.sendRedirect(uri);
    }
}