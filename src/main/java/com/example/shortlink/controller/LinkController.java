package com.example.shortlink.controller;

import com.example.shortlink.service.LinkService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/slink.ru")
public class LinkController {

    private final LinkService linkService;

    @GetMapping("/add")
    public String addLink(@RequestParam String url) {
        String DOMEN = "slink.ru";
        String code = linkService.addLink(url);
        return "Ваш новый адрес: " + DOMEN + "/" + code;
    }

    @GetMapping("/{code}")
    public void redirect(@PathVariable String code, HttpServletResponse response) throws IOException {
        String uri = linkService.getFullUrl(code);
        response.sendRedirect(uri);
    }
}
