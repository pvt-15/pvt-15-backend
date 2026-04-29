package com.example.accessingdatamysql.user.controller;

import com.example.accessingdatamysql.user.dto.UserResponse;
import com.example.accessingdatamysql.user.mapper.UserMapper;
import com.example.accessingdatamysql.user.enums.Level;
import com.example.accessingdatamysql.user.entity.User;
import com.example.accessingdatamysql.auth.enums.Provider;
import com.example.accessingdatamysql.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class MainController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public MainController(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        List<UserResponse> users = new ArrayList<>();

        for (User user : userRepository.findAll()) {
            users.add(userMapper.toUserResponse(user));
        }
        return users;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer id) {
        Optional<User> user = userRepository.findById(id);

        return user.map(userMapper::toUserResponse).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public String addNewUser(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String passwordHash,
                             @RequestParam Provider provider,
                             @RequestParam int totalPoints,
                             @RequestParam Level level,
                             @RequestParam String profileImageUrl) {
        userRepository.save(new User(name, email, passwordHash, provider, totalPoints, level, profileImageUrl));
        return "saved";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}