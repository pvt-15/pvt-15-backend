package com.example.accessingdatamysql.auth.repository;

import com.example.accessingdatamysql.auth.entity.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repository for {@link PasswordResetToken} entities.
 *
 * <p>Lookups are always performed on the SHA-256 hash of the token, never on
 * the raw value, since only the hash is stored.</p>
 */
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Integer> {

    /**
     * Finds a reset token by its SHA-256 hash.
     *
     * @param tokenHash hex-encoded SHA-256 hash of the raw token
     * @return an {@code Optional} with the token if found, otherwise empty
     */
    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    /**
     * Deletes any existing tokens for a user. Used to invalidate previous
     * outstanding reset links when a new one is requested.
     *
     * @param userId the id of the user whose tokens should be removed
     */
    void deleteByUserId(Integer userId);
}