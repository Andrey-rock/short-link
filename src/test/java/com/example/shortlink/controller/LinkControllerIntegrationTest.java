package com.example.shortlink.controller;

import com.example.shortlink.dto.CreateLinkRequest;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String domen = "http://localhost:8080";
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
    @DisplayName("Post /add - если указан алиас и срок действия," +
            "должен сохранить ссылку с заданными параметрами")
    void addLink_WhenExistAliasAndTimeLive_ShouldSaveLinkWithAlLAttribute() throws Exception {

        String requestBody = objectMapper.writeValueAsString(CREATE_LINK_REQUEST_FULL);
        String expectedResult = "Ваш новый адрес: " + domen + "/" + alias + "\n" +
                "Полный адрес: " + uri + "\n" +
                "Срок окончания действия новой ссылки: " + LocalDateTime.now().plusHours(24).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

        mockMvc.perform(post("/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string(expectedResult));
    }

    @Test
    @DisplayName("Post /add - если указан алиас и нет срока действия," +
            "должен сохранить ссылку с заданными параметрами без срока истечения")
    void addLink_WhenExistAliasAndTimeLive_ShouldSaveLinkWithoutTimeLive() throws Exception {

        String requestBody = objectMapper.writeValueAsString(CREATE_LINK_REQUEST_ALIAS);
        String expectedResult = "Ваш новый адрес: " + domen + "/" + alias + "\n" +
                "Полный адрес: " + uri + "\n" +
                "Срок окончания действия новой ссылки: неограниченно";

        mockMvc.perform(post("/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string(expectedResult));
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
                            "Ваш новый адрес: " + Pattern.quote(domen) + "/[a-zA-Z0-9]+\n" +
                                    "Полный адрес: " + Pattern.quote(uri) + "\\n" +
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
}