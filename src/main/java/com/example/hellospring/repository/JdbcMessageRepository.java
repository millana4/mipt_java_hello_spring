package com.example.hellospring.repository;

import com.example.hellospring.entity.Message;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Имплементация репозитория с использованием JdbcTemplate
@Repository
public class JdbcMessageRepository {

    // Общая часть запросов: имя автора берётся из таблицы users
    private static final String SELECT_WITH_AUTHOR =
            "SELECT m.id, m.content, m.user_id, u.username AS author, m.created_at " +
                    "FROM messages m JOIN users u ON u.id = m.user_id ";

    private final JdbcTemplate jdbcTemplate;

    // RowMapper для преобразования строки результата БД в объект Message
    private final RowMapper<Message> messageRowMapper = (rs, rowNum) -> {
        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setContent(rs.getString("content"));
        message.setUserId(rs.getLong("user_id"));
        message.setAuthor(rs.getString("author"));
        // getTimestamp, а не getObject(LocalDateTime) для максимальной совместимости с различными БД
        message.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return message;
    };

    public JdbcMessageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Message save(Message message) {
        final String sql = "INSERT INTO messages (content, user_id) VALUES (?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, message.getContent());
            ps.setLong(2, message.getUserId());
            return ps;
        }, keyHolder);

        // Установите сгенерированный ID
        if (keyHolder.getKey() != null) {
            message.setId(keyHolder.getKey().longValue());
        }

        return message;
    }

    public List<Message> saveAll(List<Message> messages) {
        final String sql = "INSERT INTO messages (content, user_id) VALUES (?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.batchUpdate(
                connection -> connection.prepareStatement(sql, new String[]{"id"}),
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Message message = messages.get(i);
                        ps.setString(1, message.getContent());
                        ps.setLong(2, message.getUserId());
                    }

                    @Override
                    public int getBatchSize() {
                        return messages.size();
                    }
                },
                keyHolder);

        // Установите сгенерированные ID в том же порядке, в котором вставлялись сообщения
        List<Map<String, Object>> keys = keyHolder.getKeyList();
        for (int i = 0; i < messages.size(); i++) {
            messages.get(i).setId(((Number) keys.get(i).get("id")).longValue());
        }

        return messages;
    }

    public Optional<Message> findById(Long id) {
        final String sql = SELECT_WITH_AUTHOR + "WHERE m.id = ?";
        List<Message> results = jdbcTemplate.query(sql, messageRowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Message> findPage(int limit, long offset) {
        // Сортировка по убыванию даты создания
        final String sql = SELECT_WITH_AUTHOR +
                "ORDER BY m.created_at DESC, m.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, messageRowMapper, limit, offset);
    }

    public List<Message> findPageByAuthor(String author, int limit, long offset) {
        final String sql = SELECT_WITH_AUTHOR +
                "WHERE u.username = ? " +
                "ORDER BY m.created_at DESC, m.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, messageRowMapper, author, limit, offset);
    }

    public int deleteById(Long id) {
        final String sql = "DELETE FROM messages WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public void clear() {
        final String sql = "DELETE FROM messages";
        jdbcTemplate.update(sql);
    }
}