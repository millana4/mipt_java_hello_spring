package com.example.hellospring;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MessageService {
    private static final int MAX_MESSAGES = 30;

    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
        initializeSampleData();
    }

    private void initializeSampleData() {
        repository.save(new Message("Hello, Spring!"));
        repository.save(new Message("In-memory storage works"));
    }

    // Возвращает не больше 30 последних сообщений, от старых к новым
    public Iterable<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        repository.findAll().forEach(messages::add);
        messages.sort(Comparator.comparing(Message::getId));

        int from = Math.max(0, messages.size() - MAX_MESSAGES);
        return messages.subList(from, messages.size());
    }

    public Message addMessage(Message message) {
        message.setId(null); // id всегда назначает репозиторий
        return repository.save(message);
    }
}