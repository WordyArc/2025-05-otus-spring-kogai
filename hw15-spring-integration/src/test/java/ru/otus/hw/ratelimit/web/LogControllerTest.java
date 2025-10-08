package ru.otus.hw.ratelimit.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LogControllerTest {


    @Autowired
    MockMvc mvc;

    @Test
    void rejectsOutOfRangeStatus() throws Exception {
        mvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":"x","ip":"1.2.3.4","route":"/a","status":99}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsMinimalValidPayload() throws Exception {
        mvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clientId":"x","ip":"1.2.3.4","route":"/a","status":429}
                                """))
                .andExpect(status().isAccepted());
    }

}
