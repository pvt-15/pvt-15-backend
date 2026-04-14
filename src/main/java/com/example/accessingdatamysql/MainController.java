package com.example.accessingdatamysql;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    private final UserRepository userRepository;

    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping(path = "/add")
    public @ResponseBody String addNewUser(@RequestParam String name, @RequestParam String email) {
        userRepository.save(new User(name, email));
        return "saved";
    }

    @GetMapping(path = "/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }
}