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
class AdminStaticPageTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void adminPageRedirectsToReactConsole() throws Exception {
        mockMvc.perform(get("/admin.html"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("<meta charset=\"utf-8\">")))
            .andExpect(content().string(containsString("content=\"0; url=/admin/index.html\"")))
            .andExpect(content().string(containsString("href=\"/admin/index.html\"")));
    }
}
