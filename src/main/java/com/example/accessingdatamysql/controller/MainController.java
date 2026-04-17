package com.example.accessingdatamysql.controller;

import com.example.accessingdatamysql.model.enums.Level;
import com.example.accessingdatamysql.model.User;
import com.example.accessingdatamysql.model.enums.Provider;
import com.example.accessingdatamysql.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class MainController {

    private final UserRepository userRepository;

    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public Iterable<User> getAllUsers(){
        return userRepository.findAll();
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
                             @RequestParam int totalPoints,
                             @RequestParam Level level,
                             @RequestParam Provider provider){
        userRepository.save(new User(name, email, passwordHash, provider, totalPoints, level));
        return "saved";
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Integer id){
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}