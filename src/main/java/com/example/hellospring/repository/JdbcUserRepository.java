package com.example.hellospring.repository;

import com.example.hellospring.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcUserRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    };

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findById(Long id) {
        final String sql = "SELECT id, username, created_at FROM users WHERE id = ?";
        List<User> results = jdbcTemplate.query(sql, userRowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<User> findByUsername(String username) {
        final String sql = "SELECT id, username, created_at FROM users WHERE username = ?";
        List<User> results = jdbcTemplate.query(sql, userRowMapper, username);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // Возвращает пользователя с таким именем, создает, если его ещё нет
    public User findOrCreate(String username) {
        final String sql = "INSERT INTO users (username) VALUES (?) ON CONFLICT (username) DO NOTHING";
        jdbcTemplate.update(sql, username);
        return findByUsername(username).orElseThrow();
    }
}