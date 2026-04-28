package com.example.accessingdatamysql.user.repository;

import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.auth.enums.Provider;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Repository interface for database access to {@code User}-entities
 *
 * <p>Interface extends {@link CrudRepository}, and therefore enables
 * basic CRUD-support for users with {@code Integer} as primary key.</p>
 *
 * <p>Gives specialised search methods which is used in authentication,
 * e.g. search by email and provider. </p>
 */
public interface UserRepository extends CrudRepository<User, Integer> {

    /**
     * Fetches a user based on email.
     *
     * @param email email to search for
     * @return a {@code Optional}, containing a user if a match is found,
     *         otherwise an empty {@code Optional}
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if there exists a user with the given email.
     *
     * @param email email to search for
     * @return {@code true} if a user with this email is found,
     *         otherwise {@code false}
     */
    boolean existsByEmail(String email);

    /**
     * Fetches a user based on provider and provider-specific user-ID
     *
     * @param provider the external provider the account belongs to
     * @param providerUserId the providers unique ID for the user
     * @return a {@code Optional}, containing a user if a match is found,
     *         otherwise an empty {@code Optional}
     */
    Optional<User> findByProviderAndProviderUserId(Provider provider, String providerUserId);
}