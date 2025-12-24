package com.example.shortlink.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "links")
public class LinkEntity {

    @Id
    private String code;

    private String link;

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