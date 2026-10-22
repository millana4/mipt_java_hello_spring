package com.example.hellospring;

import java.util.Objects;

public class Message {
    private Long id;
    private String content;
    private String author;

    // Конструкторы
    public Message() {}

    public Message(String content) {
        this.content = content;
    }

    public Message(String author, String content) {
        this.author = author;
        this.content = content;
    }

    // Геттеры и сеттеры
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message message)) {
            return false;
        }
        return Objects.equals(id, message.id)
                && Objects.equals(content, message.content)
                && Objects.equals(author, message.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, content, author);
    }
}