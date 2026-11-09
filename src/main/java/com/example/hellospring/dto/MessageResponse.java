package com.example.hellospring.dto;

import com.example.hellospring.entity.Message;

// только данные, нужные клиенту, без полного объекта
public record MessageResponse(Long id, String content, String authorUsername) {

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getContent(),
                message.getAuthor().getUsername()
        );
    }
}