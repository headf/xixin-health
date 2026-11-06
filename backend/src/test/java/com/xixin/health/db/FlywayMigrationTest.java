package com.xixin.health.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FlywayMigrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void seedsRequiredRoles() {
        Integer count = jdbcTemplate.queryForObject("select count(*) from sys_role", Integer.class);

        assertThat(count).isGreaterThanOrEqualTo(6);
    }
}
