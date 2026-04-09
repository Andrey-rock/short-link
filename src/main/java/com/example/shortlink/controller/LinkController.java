package com.example.shortlink.controller;

import com.example.shortlink.dto.CreateLinkRequest;
import com.example.shortlink.entity.LinkEntity;
import com.example.shortlink.exception.AliasAlreadyExistsException;
import com.example.shortlink.exception.InvalidAliasException;
import com.example.shortlink.exception.LinkExpiredException;
import com.example.shortlink.service.ILinkService;
import jakarta.servlet.http.HttpServletRequest;
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
    public String addLink(@Valid @RequestBody CreateLinkRequest request,
                          HttpServletRequest httpServletRequest) throws AliasAlreadyExistsException, InvalidAliasException {
        String PATTERN = "dd.MM.yyyy HH:mm";
        String MESSAGE = "Ваш новый адрес: %s/%s%nПолный адрес: %s%nСрок окончания действия новой ссылки: %s";
        String domen = httpServletRequest.getRequestURL()
                .substring(0, httpServletRequest.getRequestURL().toString().indexOf("/add"));
        LinkEntity entity = ILinkService.addLink(request);
        String expires = entity.getExpiresAt() != null
                ? entity.getExpiresAt().format(DateTimeFormatter.ofPattern(PATTERN))
                : "неограниченно";
        return String.format(MESSAGE,
                domen,
                entity.getCode().trim(),
                entity.getLink(),
                expires);
    }

    @GetMapping("/{code}")
    public void redirect(@PathVariable String code, HttpServletResponse response) throws IOException,
            LinkExpiredException, LinkExpiredException {
        String uri = ILinkService.getFullUrl(code);
        response.sendRedirect(uri);
    }
}