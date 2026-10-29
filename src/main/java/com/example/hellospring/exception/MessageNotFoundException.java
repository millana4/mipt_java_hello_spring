package com.example.hellospring.exception;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(Long id) {
        super("Сообщение с id " + id + " не найдено");
    }
}