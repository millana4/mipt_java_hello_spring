package com.example.hellospring.repository;

import com.example.hellospring.entity.Message;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Пример кастомного запроса для борьбы с проблемой N+1
    // JOIN FETCH гарантирует, что автор будет загружен одним запросом
    // @Query("SELECT m FROM Message m JOIN FETCH m.author ORDER BY m.createdAt DESC LIMIT 30")
    // List<Message> findLatestWithAuthors();

    // Решение N+1 без @Query: запрос строится по имени метода,
    // @EntityGraph загружает автора в том же запросе через JOIN
    @EntityGraph(attributePaths = "author")
    List<Message> findTop30WithAuthorByOrderByCreatedAtDesc();

    // Стандартный метод, который вызовет N+1 или LazyInitializationException,
    // если FetchType.LAZY
    List<Message> findTop30ByOrderByCreatedAtDesc();

    // Загружает сообщение вместе с автором одним запросом
    @EntityGraph(attributePaths = "author")
    Optional<Message> findWithAuthorById(Long id);
}