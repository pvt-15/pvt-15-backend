package com.example.accessingdatamysql;

import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public String addNewUser(@RequestParam String name,
                             @RequestParam String email){
        userRepository.save(new User(name, email));
        return "saved";
    }
}