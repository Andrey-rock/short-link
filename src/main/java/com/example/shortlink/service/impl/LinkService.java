package com.example.shortlink.service.impl;

import com.example.shortlink.dto.CreateLinkRequest;
import com.example.shortlink.entity.LinkEntity;
import com.example.shortlink.exception.AliasAlreadyExistsException;
import com.example.shortlink.exception.InvalidAliasException;
import com.example.shortlink.exception.LinkExpiredException;
import com.example.shortlink.exception.LinkNotFoundException;
import com.example.shortlink.repository.LinkRepository;
import com.example.shortlink.service.ILinkService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LinkService implements ILinkService {

    private final LinkRepository linkRepository;


    @Override
    @Transactional
    public LinkEntity addLink(CreateLinkRequest request) {

        Optional<LinkEntity> existLink = linkRepository.findByLink(request.url());
        if (existLink.isPresent()) {
            return existLink.get();
        }

        String code = request.alias() != null && !request.alias().isEmpty() ?
                validateAndReserveAlias(request.alias()) : generateUniqueCode();

        OffsetDateTime expiresAt = null;
        if (request.expiresInHours() != null) {
            expiresAt = OffsetDateTime.now().plusHours(request.expiresInHours());
        }

        LinkEntity link = LinkEntity.builder()
                .code(code)
                .link(request.url())
                .expiresAt(expiresAt)
                .build();

        return linkRepository.save(link);
    }

    @Override
    public String getFullUrl(String code) {
        LinkEntity link = linkRepository.findByCode(code).orElseThrow(() -> new LinkNotFoundException("страница не найдена"));

        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new LinkExpiredException("Ссылка истекла");
        }
        return link.getLink();
    }

    private String validateAndReserveAlias(String alias) {
        // Проверяем формат
        if (!alias.matches("^[a-zA-Z0-9]{3,10}$")) {
            throw new InvalidAliasException("Alias должен содержать 3-10 букв или цифр");
        }

        // Проверяем, не занят ли
        if (linkRepository.existsByCode(alias)) {
            throw new AliasAlreadyExistsException("Alias '" + alias + "' уже занят");
        }

        return alias;
    }

    private String generateUniqueCode() {
        String code;
        int length = 6;
        int attempts = 0;
        do {
            if (attempts++ > 10) {
                throw new RuntimeException("Не удалось сгенерировать уникальный код");
            }
            code = generateRandomCode(length);
        } while (linkRepository.existsByCode(code));
        return code;
    }

    private @NotNull String generateRandomCode(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}