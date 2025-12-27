package com.example.shortlink.service;

import com.example.shortlink.dto.CreateLinkRequest;
import com.example.shortlink.entity.LinkEntity;
import com.example.shortlink.exception.AliasAlreadyExistsException;
import com.example.shortlink.exception.InvalidAliasException;
import com.example.shortlink.exception.LinkExpiredException;
import com.example.shortlink.exception.LinkNotFoundException;
import com.example.shortlink.repository.LinkRepository;
import com.example.shortlink.service.impl.LinkServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Юнит-тесты для сервиса генерации ссылок")
public class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private LinkServiceImpl linkService;

    LocalDateTime now;
    String domen;
    String uri;
    String code;
    CreateLinkRequest requestWithoutAliasAndTime, requestWithoutAlias, requestWithoutTime,
            requestWithAliasAndTime, requestWithIncorrectAlias;
    LinkEntity testLink, oldTestLink;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        domen = "http://localhost:8080";
        uri = "https://test.com/qwerty123456789";
        code = "test1";
        requestWithoutAliasAndTime = new CreateLinkRequest(uri, null, null);
        requestWithoutAlias = new CreateLinkRequest(uri, null, 24);
        requestWithoutTime = new CreateLinkRequest(uri, "alias", null);
        requestWithAliasAndTime = new CreateLinkRequest(uri, "alias", 24);
        requestWithIncorrectAlias = new CreateLinkRequest(uri, "incorrectAlias", 24);
        testLink = new LinkEntity(code, uri, now, LocalDateTime.now().plusHours(1));
        oldTestLink = new LinkEntity(code, uri, now, LocalDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("Тест успешного создания ссылки без алиаса и времени действия")
    void addLink_ShouldSaveLinkWithGenCodeWithoutTimeLiveAndReturnShortLink() {

        String shortLink = linkService.addLink(requestWithoutAliasAndTime);

        assertEquals(6, shortLink.length());
        verify(linkRepository).save(argThat(linkEntity ->
                linkEntity.getLink().equals(uri) &&
                        linkEntity.getCode().matches("[a-zA-Z0-9]{6}") &&
                        linkEntity.getExpiresAt() == null));
    }

    @Test
    @DisplayName("Тест успешного создания ссылки без алиаса c временем действия")
    void addLink_ShouldSaveLinkWithGenCodeAndTimeLiveAndReturnShortLink() {

        when(linkRepository.findByLink(anyString())).thenReturn(Optional.empty());

        String shortLink = linkService.addLink(requestWithoutAlias);

        assertEquals(6, shortLink.length());
        verify(linkRepository).save(argThat(linkEntity ->
                linkEntity.getLink().equals(uri) &&
                        linkEntity.getCode().matches("[a-zA-Z0-9]{6}") &&
                        linkEntity.getExpiresAt().truncatedTo(ChronoUnit.SECONDS)
                                .equals(LocalDateTime.now().plusHours(24).truncatedTo(ChronoUnit.SECONDS))));
    }

    @Test
    @DisplayName("Тест успешного создания ссылки c алиасом без времени действия")
    void addLink_ShouldSaveLinkWithAliasWithoutTimeLiveAndReturnShortLink() {

        String shortLink = linkService.addLink(requestWithoutTime);

        assertEquals("alias", shortLink);
        verify(linkRepository).save(argThat(linkEntity ->
                linkEntity.getLink().equals(uri) &&
                        linkEntity.getCode().equals("alias") &&
                        linkEntity.getExpiresAt() == null));
    }

    @Test
    @DisplayName("Тест успешного создания ссылки со всеми параметрами")
    void addLink_ShouldSaveLinkWithAllAttributesLiveAndReturnShortLink() {

        String shortLink = linkService.addLink(requestWithAliasAndTime);

        assertEquals("alias", shortLink);
        verify(linkRepository).save(argThat(linkEntity ->
                linkEntity.getLink().equals(uri) &&
                        linkEntity.getCode().equals("alias") &&
                        linkEntity.getExpiresAt().truncatedTo(ChronoUnit.SECONDS)
                                .equals(LocalDateTime.now().plusHours(24).truncatedTo(ChronoUnit.SECONDS))));
    }

    @Test
    @DisplayName("Тест создания ссылки с слишком длинным алиасом")
    void addLink_ShouldReturnInvalidAliasException() {

        assertThrows(InvalidAliasException.class, () -> linkService.addLink(requestWithIncorrectAlias));
    }

    @Test
    @DisplayName("Тест создания ссылки с занятым алиасом")
    void addLink_ShouldReturnAliasAlreadyExistsException() {

        when(linkRepository.existsByCode("alias")).thenReturn(true);

        assertThrows(AliasAlreadyExistsException.class, () -> linkService.addLink(requestWithAliasAndTime));
    }

    @Test
    @DisplayName("Тест успешного запроса длинной ссылки")
    void getFullUrl_ShouldReturnFullUrl() {

        when(linkRepository.findByCode(code)).thenReturn(Optional.ofNullable(testLink));

        String result = linkService.getFullUrl(code);

        assertEquals(uri, result);
    }

    @Test
    @DisplayName("Тест запроса длинной ссылки по несуществующему коду")
    void getFullUrl_ShouldReturnLinkNotFoundException() {

        when(linkRepository.findByCode(code)).thenReturn(Optional.empty());

        assertThrows(LinkNotFoundException.class, () -> linkService.getFullUrl(code));
    }

    @Test
    @DisplayName("Тест запроса длинной ссылки с истекшим сроком действия")
    void getFullUrl_ShouldReturnLinkExpiredException() {

        when(linkRepository.findByCode(code)).thenReturn(Optional.ofNullable(oldTestLink));

        assertThrows(LinkExpiredException.class, () -> linkService.getFullUrl(code));
    }
}