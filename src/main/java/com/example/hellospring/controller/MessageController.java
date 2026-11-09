package com.example.hellospring.controller;

import com.example.hellospring.dto.MessageResponse;
import com.example.hellospring.entity.Message;
import com.example.hellospring.service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// DTO для удобного приёма данных от клиента
record CreateMessageRequest(String content, String username) {}

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    // GET /api/messages/optimized. Рекомендуемый подход: 1 SQL запрос для сообщений и авторов.
    @GetMapping("/optimized")
    public List<Message> getMessagesOptimized() {
        return service.getLatestMessagesOptimized();
    }

    // GET /api/messages/nplus1. Проблема N+1: будет выполнено N+1 запросов к базе данных.
    @GetMapping("/nplus1")
    public List<Message> getMessagesNPlus1() {
        return service.getLatestMessagesNPlus1();
    }

    // POST /api/messages. Добавляет новое сообщение.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Message createMessage(@RequestBody CreateMessageRequest request) {
        String username = (request.username() == null || request.username().trim().isEmpty())
                ? "Anonymous"
                : request.username();

        return service.saveMessage(request.content(), username);
    }

    // GET /api/messages/fail/1. Демонстрация LazyInitializationException (Proxy Exception)
    @GetMapping("/fail/{id}")
    public ResponseEntity<?> getMessageToFail(@PathVariable Long id) {
        try {
            MessageResponse message = service.getMessageToFail(id);
            // Здесь происходит попытка сериализации в JSON
            // Если автор LAZY и он не загружен, то будет ошибка
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            String errorMsg = "Ошибка! Проверьте консоль. Вероятно, LazyInitializationException (Proxy Exception), " +
                    "потому что вы пытаетесь получить LAZY-поле вне транзакции. " +
                    "Для решения: поместите метод getMessageToFail в @Transactional или используйте JOIN FETCH.";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorMsg);
        }
    }
}