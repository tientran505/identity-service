package com.example.identity_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpiredTokenRepository
        extends JpaRepository<com.example.identity_service.entity.ExpiredToken, String> {}
