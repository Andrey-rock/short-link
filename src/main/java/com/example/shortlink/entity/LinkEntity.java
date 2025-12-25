package com.example.shortlink.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "links")
public class LinkEntity {

    @Id
    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "link", nullable = false, length = 2048)
    private String link;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LinkEntity that)) return false;
        return Objects.equals(code, that.code) && Objects.equals(link, that.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, link);
    }
}