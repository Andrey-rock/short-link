package com.example.shortlink.repository;

import com.example.shortlink.entity.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<LinkEntity, String> {
    Optional<LinkEntity> findByCode(String code);
}
