package com.example.accessingdatamysql.repository;

import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Provider;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndProvider(String email, Provider provider);
}