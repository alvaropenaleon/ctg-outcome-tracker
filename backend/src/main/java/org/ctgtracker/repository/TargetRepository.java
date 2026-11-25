package org.ctgtracker.repository;

import java.util.List;

import org.ctgtracker.model.Target;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class TargetRepository {

    private final JdbcClient jdbcClient;

    public TargetRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Target> findAll() {
        return jdbcClient.sql("""
                SELECT id, category, title, definition, source_url
                FROM targets
                ORDER BY display_order
                """)
                .query(Target.class)
                .list();
    }
}
