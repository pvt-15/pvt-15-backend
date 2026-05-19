package com.example.accessingdatamysql.auth.repository;

import com.example.accessingdatamysql.auth.entity.RevokedToken;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;

public interface RevokedTokenRepository extends CrudRepository<RevokedToken, Integer> {

    boolean existsByJti(String jti);

    void deleteByExpiresAtBefore(Instant instant);
}