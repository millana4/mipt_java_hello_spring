package com.example.hellospring.controller;

import com.example.hellospring.dto.MessageResponse;
import com.example.hellospring.entity.Message;
import com.example.hellospring.service.MessageService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

record CreateMessageRequest(String content, String username) {}

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageService service;
    private final Counter messagesCreatedCounter;

    public MessageController(MessageService service, MeterRegistry meterRegistry) {
        this.service = service;
        this.messagesCreatedCounter = Counter.builder("messages.created")
                .description("Number of messages successfully saved to the database")
                .register(meterRegistry);
    }

    // GET /api/messages — последние сообщения с авторами
    @GetMapping
    public List<Message> getMessages() {
        return service.getLatestMessagesOptimized();
    }

    // GET /api/messages/optimized. 1 SQL запрос для сообщений и авторов.
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

        Message message = service.saveMessage(request.content(), username);
        // Транзакция сервиса уже зафиксирована: сообщение точно сохранено в БД
        messagesCreatedCounter.increment();
        return message;
    }

    // GET /api/messages/fail/1. Демонстрация LazyInitializationException
    @GetMapping("/fail/{id}")
    public ResponseEntity<?> getMessageToFail(@PathVariable Long id) {
        try {
            MessageResponse message = service.getMessageToFail(id);
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