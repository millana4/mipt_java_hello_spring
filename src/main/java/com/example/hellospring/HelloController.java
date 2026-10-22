package com.example.hellospring;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private final MessageService service;

    public HelloController(MessageService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String hello() {
        return "Hello, World!";
    }

    @GetMapping("/messages")
    public Iterable<Message> getMessages() {
        return service.getAllMessages();
    }

    @PostMapping("/messages")
    public Message sendMessage(@RequestBody Message message) {
        return service.addMessage(message);
    }
}