package com.xixin.health.staticclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TestClientStaticPageTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void servesTestClientPage() throws Exception {
        mockMvc.perform(get("/test-client.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Xixin API Test Console")))
            .andExpect(content().string(containsString("const API_BASE = '/api/v1'")));
    }
}
