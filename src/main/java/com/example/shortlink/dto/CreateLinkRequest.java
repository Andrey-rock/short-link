package com.example.shortlink.dto;

import jakarta.validation.constraints.*;

public record CreateLinkRequest(
        @NotBlank
        String url,
        @Pattern(regexp = "^[a-zA-Z0-9]{3,10}$", message = "Alias должен быть 3-10 символов (буквы и цифры)")
        String alias,

        @Min(value = 1, message = "Время жизни должно быть от 1 часа")
        @Max(value = 8760, message = "Время жизни не более 1 года (8760 часов)")
        Integer expiresInHours) {
}
