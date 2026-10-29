package com.example.hellospring.service;

import com.example.hellospring.entity.Message;
import com.example.hellospring.exception.MessageNotFoundException;
import com.example.hellospring.repository.JdbcMessageRepository;
import com.example.hellospring.repository.JdbcUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_BATCH_SIZE = 1000;

    private final JdbcMessageRepository repository;
    private final JdbcUserRepository userRepository;

    public MessageService(JdbcMessageRepository repository, JdbcUserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        // Инициализировать БД тестовыми данными при старте
        initializeSampleData();
    }

    private void initializeSampleData() {
        repository.clear();
        saveMessage(new Message("Hello, JDBC Spring!", "System"));
        saveMessage(new Message("Now using Postgres database via JdbcTemplate.", "Admin"));
    }

    public List<Message> getMessages(int page, int size, String author) {
        if (page < 0) {
            throw new IllegalArgumentException("Номер страницы не может быть отрицательным");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Размер страницы должен быть от 1 до " + MAX_PAGE_SIZE);
        }
        long offset = (long) page * size;

        if (author != null && !author.isBlank()) {
            return repository.findPageByAuthor(author.trim(), size, offset);
        }
        return repository.findPage(size, offset);
    }

    public Message getMessageById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new MessageNotFoundException(id));
    }

    @Transactional
    public Message saveMessage(Message message) {
        if (message.getContent() == null || message.getContent().isBlank()) {
            throw new IllegalArgumentException("Текст сообщения не может быть пустым");
        }
        Long userId = userRepository.findOrCreate(message.getAuthor()).getId();
        message.setUserId(userId);
        return repository.save(message);
    }

    @Transactional
    public List<Message> saveMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Список сообщений не может быть пустым");
        }
        if (messages.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("За один раз можно создать не больше " + MAX_BATCH_SIZE + " сообщений");
        }
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message == null || message.getContent() == null || message.getContent().isBlank()) {
                throw new IllegalArgumentException("Сообщение №" + (i + 1) + ": текст не может быть пустым");
            }
        }

        Map<String, Long> userIds = new HashMap<>();
        for (Message message : messages) {
            Long userId = userIds.computeIfAbsent(message.getAuthor(),
                    name -> userRepository.findOrCreate(name).getId());
            message.setUserId(userId);
        }

        return repository.saveAll(messages);
    }

    public void deleteMessage(Long id) {
        int deleted = repository.deleteById(id);
        if (deleted == 0) {
            throw new MessageNotFoundException(id);
        }
    }
}