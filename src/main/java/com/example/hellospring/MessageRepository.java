package com.example.hellospring;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class MessageRepository {
    private final ConcurrentHashMap<Long, Message> storage = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    public Message save(Message message) {
        if (message.getId() == null) {
            message.setId(idCounter.incrementAndGet());
        }
        storage.put(message.getId(), message);
        return message;
    }

    public Message findById(Long id) {
        return storage.get(id);
    }

    public Iterable<Message> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }

    public void clear() {
        storage.clear();
    }
}