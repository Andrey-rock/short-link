package com.example.shortlink.controller;

import com.example.shortlink.dto.CreateLinkRequest;
import com.example.shortlink.entity.LinkEntity;
import com.example.shortlink.repository.LinkRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
@DisplayName("Интеграционные тесты")
@Transactional
public class LinkControllerIntegrationTest {

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private MockMvc mockMvc;

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:17.1-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String domen = "http://localhost";
    private static final String uri = "https://test.com/qwerty123456789";
    private static final String alias = "alias";
    private static final String invalidAliasShort = "ab";
    private static final String invalidAliasLong = "abcdefghijk";
    private static final String invalidAliasSpecial = "alias!@#";

    private static final CreateLinkRequest CREATE_LINK_REQUEST_FULL = new CreateLinkRequest(uri, alias, 24);
    private static final CreateLinkRequest CREATE_LINK_REQUEST_ALIAS = new CreateLinkRequest(uri, alias, null);
    private static final CreateLinkRequest CREATE_LINK_REQUEST_NO_ATTRIBUTE = new CreateLinkRequest(uri, null, null);
    private static final CreateLinkRequest CREATE_LINK_REQUEST_EMPTY_URL = new CreateLinkRequest("", alias, null);
    private static final CreateLinkRequest CREATE_LINK_REQUEST_SHORT_ALIAS = new CreateLinkRequest(uri, invalidAliasShort, null);
    private static final CreateLinkRequest CREATE_LINK_REQUEST_LONG_ALIAS = new CreateLinkRequest(uri, invalidAliasLong, null);
    private static final CreateLinkRequest CREATE_LINK_REQUEST_SPECIAL_ALIAS = new CreateLinkRequest(uri, invalidAliasSpecial, null);

    @AfterEach
    void tearDown() {
        linkRepository.deleteAll();
    }

    @Test
    @DisplayName("Post /add - если указан алиас и срок действия, должен сохранить ссылку с заданными параметрами")
    void addLink_WhenExistAliasAndTimeLive_ShouldSaveLinkWithAlLAttribute() throws Exception {
        String requestBody = objectMapper.writeValueAsString(CREATE_LINK_REQUEST_FULL);

        mockMvc.perform(post("/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string(matchesPattern(
                        "Ваш новый адрес: " + Pattern.quote(domen) + "/" + alias + "\r?\n" +
                                "Полный адрес: " + Pattern.quote(uri) + "\r?\n" +
                                "Срок окончания действия новой ссылки: \\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}"
                )));
    }

    @Test
    @DisplayName("Post /add - если указан алиас и нет срока действия," +
            "должен сохранить ссылку с заданными параметрами без срока истечения")
    void addLink_WhenExistAliasAndTimeLive_ShouldSaveLinkWithoutTimeLive() throws Exception {

        String requestBody = objectMapper.writeValueAsString(CREATE_LINK_REQUEST_ALIAS);

        mockMvc.perform(post("/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string(matchesPattern(
                        "Ваш новый адрес: " + Pattern.quote(domen) + "/" + alias + "\r?\n" +
                                "Полный адрес: " + Pattern.quote(uri) + "\r?\n" +
                                "Срок окончания действия новой ссылки: неограниченно"
                )));
    }

    @Test
    @DisplayName("Post /add - если алиас и срок действия не указаны," +
            "должен сохранить ссылку с сгенерированным коротким кодом и без срока истечения")
    void addLink_WhenExistAliasAndTimeLive_ShouldSaveLinkWithGeneratedCode() throws Exception {

        String requestBody = objectMapper.writeValueAsString(CREATE_LINK_REQUEST_NO_ATTRIBUTE);

        mockMvc.perform(post("/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andDo(result -> {
                    String response = result.getResponse().getContentAsString();

                    // Проверяем весь формат ответа
                    assertThat(response).matches(
                            "Ваш новый адрес: " + Pattern.quote(domen) + "/[a-zA-Z0-9]+\r?\n" +
                                    "Полный адрес: " + Pattern.quote(uri) + "\r?\n" +
                                    "Срок окончания действия новой ссылки: неограниченно"
                    );
                });
    }

    @Test
    @DisplayName("Post /add - если алиас короче 3 символов, должен вернуть ошибку")
    void addLink_WhenAliasTooShort_ShouldReturnError() throws Exception {
        String requestBody = objectMapper.writeValueAsString(CREATE_LINK_REQUEST_SHORT_ALIAS);

        mockMvc.perform(post("/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("{alias=Alias должен быть 3-10 символов (буквы и цифры)}"));
    }

    @Test
    @DisplayName("Post /add - если алиас длиннее 10 символов, должен вернуть ошибку")
    void addLink_WhenAliasTooLong_ShouldReturnError() throws Exception {
        String requestBody = objectMapper.writeValueAsString(CREATE_LINK_REQUEST_LONG_ALIAS);

        mockMvc.perform(post("/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("{alias=Alias должен быть 3-10 символов (буквы и цифры)}"));
    }

    @Test
    @DisplayName("Post /add - если алиас содержит специальные символы, должен вернуть ошибку")
    void addLink_WhenAliasContainsSpecialCharacters_ShouldReturnError() throws Exception {
        String requestBody = objectMapper.writeValueAsString(CREATE_LINK_REQUEST_SPECIAL_ALIAS);

        mockMvc.perform(post("/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("{alias=Alias должен быть 3-10 символов (буквы и цифры)}"));
    }

    @Test
    @DisplayName("Post /add - если URL пустой, должен вернуть ошибку валидации")
    void addLink_WhenUrlEmpty_ShouldReturnValidationError() throws Exception {
        String requestBody = objectMapper.writeValueAsString(CREATE_LINK_REQUEST_EMPTY_URL);

        mockMvc.perform(post("/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("GET /{code} - если код существует и не истек, должен выполнить редирект")
    void redirect_WhenCodeExistsAndNotExpired_ShouldRedirect() throws Exception {
        // Создаем ссылку в базе данных
        LinkEntity link = LinkEntity.builder()
                .code("valid123")
                .link(uri)
                .createdAt(OffsetDateTime.now())
                .build();
        linkRepository.save(link);

        mockMvc.perform(get("/{code}", "valid123"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(uri));
    }

    @Test
    @DisplayName("GET /{code} - если код существует с истекшим сроком, должен вернуть ошибку")
    void redirect_WhenCodeExistsButExpired_ShouldReturnError() throws Exception {
        // Создаем ссылку с истекшим сроком
        LinkEntity link = LinkEntity.builder()
                .code("expired1")
                .link(uri)
                .createdAt(OffsetDateTime.now().minusDays(1))
                .expiresAt(OffsetDateTime.now().minusHours(1))
                .build();
        linkRepository.save(link);

        mockMvc.perform(get("/{code}", "expired1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ссылка истекла"));
    }

    @Test
    @DisplayName("GET /{code} - если код не существует, должен вернуть 404")
    void redirect_WhenCodeNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/{code}", "nonexist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("страница не найдена"));
    }

    @Test
    @DisplayName("GET /{code} - если код существует с будущим сроком истечения, должен выполнить редирект")
    void redirect_WhenCodeExistsWithFutureExpiration_ShouldRedirect() throws Exception {
        // Создаем ссылку с будущим сроком истечения
        LinkEntity link = LinkEntity.builder()
                .code("future12")
                .link(uri)
                .createdAt(OffsetDateTime.now())
                .expiresAt(OffsetDateTime.now().plusHours(48))
                .build();
        linkRepository.save(link);

        mockMvc.perform(get("/{code}", "future12"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(uri));
    }

    @Test
    @DisplayName("GET /{code} - если код существует без срока истечения, должен выполнить редирект")
    void redirect_WhenCodeExistsWithoutExpiration_ShouldRedirect() throws Exception {
        // Создаем ссылку без срока истечения
        LinkEntity link = LinkEntity.builder()
                .code("noexpire")
                .link(uri)
                .createdAt(OffsetDateTime.now())
                .expiresAt(null) // Без срока истечения
                .build();
        linkRepository.save(link);

        mockMvc.perform(get("/{code}", "noexpire"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(uri));
    }

    @Test
    @DisplayName("GET /{code} - проверка редиректа для разных URL")
    void redirect_WhenCodeExistsWithDifferentUrls_ShouldRedirectCorrectly() throws Exception {
        // Тестируем различные URL
        String[] testUrls = {
                "https://google.com",
                "https://github.com",
                "http://example.com",
                "https://stackoverflow.com/questions/123456"
        };

        for (int i = 0; i < testUrls.length; i++) {
            String code = "test" + i;
            String testUrl = testUrls[i];

            LinkEntity link = LinkEntity.builder()
                    .code(code)
                    .link(testUrl)
                    .createdAt(OffsetDateTime.now())
                    .build();
            linkRepository.save(link);

            mockMvc.perform(get("/{code}", code))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl(testUrl));

            linkRepository.deleteAll();
        }
    }

    @Test
    @DisplayName("GET /{code} - проверка чувствительности к регистру")
    void redirect_WhenCodeWithDifferentCase_ShouldBeCaseSensitive() throws Exception {

        LinkEntity link = LinkEntity.builder()
                .code("testcode")
                .link(uri)
                .createdAt(OffsetDateTime.now())
                .build();
        linkRepository.save(link);

        // Попытка доступа с разным регистром должна вернуть 404
        mockMvc.perform(get("/{code}", "TESTCODE"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/{code}", "TestCode"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/{code}", "testcode"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(uri));
    }
}