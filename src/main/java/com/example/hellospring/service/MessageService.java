package com.example.hellospring.service;

import com.example.hellospring.dto.MessageResponse;
import com.example.hellospring.entity.Message;
import com.example.hellospring.entity.User;
import com.example.hellospring.repository.MessageRepository;
import com.example.hellospring.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.hibernate.Hibernate;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        initializeSampleData();
    }

    private void initializeSampleData() {
        // Создание пользователей
        User user1 = userRepository.save(new User("Alice"));
        User user2 = userRepository.save(new User("Bob"));

        // Создание сообщений
        messageRepository.save(new Message("Hello, JPA and Hibernate!", user1));
        messageRepository.save(new Message("This uses Spring Data JPA interface.", user2));
        messageRepository.save(new Message("Relationships are now managed by ORM.", user1));
    }

    @Transactional
    public Message saveMessage(String content, String username) {
        // 1. Найти существующего пользователя или создать нового
        User author = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new User(username)));

        // 2. Создать и сохранить сообщение
        Message message = new Message(content, author);
        return messageRepository.save(message);
    }

    //Вариант А: Проблема N+1 -- один запрос за сообщениями + по запросу на каждого автора
    @Transactional
    public List<Message> getLatestMessagesNPlus1() {
        System.out.println("--- ЗАПРОС N+1 ---");
        List<Message> messages = messageRepository.findTop30ByOrderByCreatedAtDesc();

        // Принудительная загрузка автора для избежания Proxy Exception в контроллере.
        messages.forEach(m -> m.getAuthor().getUsername());

        return messages;
    }

    /**
     * Вариант B: Решение проблемы N+1 с помощью JOIN FETCH.
     * Все данные загружаются ОДНИМ запросом.

    @Transactional
    public List<Message> getLatestMessagesOptimized() {
        System.out.println("--- ОПТИМИЗИРОВАННЫЙ ЗАПРОС JOIN FETCH ---");
        return messageRepository.findLatestWithAuthors();
    }
     */


    /**
     * Вариант B: Решение проблемы N+1 с помощью @EntityGraph.
     * Все данные загружаются ОДНИМ запросом.
     */
    @Transactional
    public List<Message> getLatestMessagesOptimized() {
        System.out.println("--- ОПТИМИЗИРОВАННЫЙ ЗАПРОС @EntityGraph ---");
        return messageRepository.findTop30WithAuthorByOrderByCreatedAtDesc();
    }

    /**
    // Демонстрация LazyInitializationException: метод НЕ помечен @Transactional
    public Message getMessageToFail(Long id) {
        return messageRepository.findById(id).orElseThrow();
    }
     */
    // Способ JPA: транзакция держит сессию открытой, пока выполняется метод
    /**
    @Transactional
    public Message getMessageToFail(Long id) {
        Message message = messageRepository.findById(id).orElseThrow();
        Hibernate.initialize(message.getAuthor());
        return message;
    }
    */

    // Способ DTO: автор загружается тем же запросом, а наружу отдаётся DTO без объектов
    public MessageResponse getMessageToFail(Long id) {
        Message message = messageRepository.findWithAuthorById(id).orElseThrow();
        return MessageResponse.from(message);
    }
}