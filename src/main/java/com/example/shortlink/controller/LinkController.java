package com.example.shortlink.controller;

import com.example.shortlink.dto.CreateLinkRequest;
import com.example.shortlink.entity.LinkEntity;
import com.example.shortlink.service.ILinkService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@Validated
public class LinkController {

    private final ILinkService ILinkService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/add")
    public String addLink(@Valid @RequestBody CreateLinkRequest request) {
        String domen = "http://localhost:8080";
        LinkEntity entity = ILinkService.addLink(request);
        String expires = entity.getExpiresAt() != null
                ? entity.getExpiresAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                : "неограниченно";
        return "Ваш новый адрес: " + domen + "/" + entity.getCode().trim() + "\n" +
                "Полный адрес: " + entity.getLink() + "\n" +
                "Срок окончания действия новой ссылки: " + expires;
    }

    @GetMapping("/{code}")
    public void redirect(@PathVariable String code, HttpServletResponse response) throws IOException {
        String uri = ILinkService.getFullUrl(code);
        response.sendRedirect(uri);
    }
}