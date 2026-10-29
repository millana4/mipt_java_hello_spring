package com.example.hellospring.controller;

import com.example.hellospring.entity.Message;
import com.example.hellospring.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping
    public List<Message> getMessages(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "30") int size,
                                     @RequestParam(required = false) String author) {
        return service.getMessages(page, size, author);
    }

    @GetMapping("/{id}")
    public Message getMessage(@PathVariable Long id) {
        return service.getMessageById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201 Created
    public Message createMessage(@RequestBody Message message) {
        // автор указан, если нет —  «Anonymous»
        if (message.getAuthor() == null || message.getAuthor().trim().isEmpty()) {
            message.setAuthor("Anonymous");
        }
        return service.saveMessage(message);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
    public void deleteMessage(@PathVariable Long id) {
        service.deleteMessage(id);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED) // Возвращаем 201 Created
    public List<Message> createMessages(@RequestBody List<Message> messages) {
        // если автор не указан Anonymous
        for (Message message : messages) {
            if (message != null && (message.getAuthor() == null || message.getAuthor().trim().isEmpty())) {
                message.setAuthor("Anonymous");
            }
        }
        return service.saveMessages(messages);
    }
}