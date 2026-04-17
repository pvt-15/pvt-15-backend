package com.example.accessingdatamysql.controller;

import com.example.accessingdatamysql.dto.UserResponse;
import com.example.accessingdatamysql.model.enums.Level;
import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Provider;
import com.example.accessingdatamysql.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class MainController {

    private final UserRepository userRepository;

    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<UserResponse> getAllUsers(){
        List<UserResponse> users = new ArrayList<>();

        for(User user : userRepository.findAll()){
            users.add(toUserResponse(user));
        }
        return users;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id){
        Optional<User> user = userRepository.findById(id);

        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public String addNewUser(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String passwordHash,
                             @RequestParam Provider provider,
                             @RequestParam String providerUserId,
                             @RequestParam int totalPoints,
                             @RequestParam Level level){
        userRepository.save(new User(name, email, passwordHash, provider, providerUserId, totalPoints, level));
        return "saved";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id){
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private UserResponse toUserResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProvider(),
                user.getTotalPoints(),
                user.getLevel());
    }
}