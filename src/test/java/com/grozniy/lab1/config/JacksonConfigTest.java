package com.grozniy.lab1.config;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void htmlSensitiveCharactersAreEscapedInJsonOutput() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("content", "<script>alert('xss') & more</script>"));

        assertThat(json)
                .contains("\\u003C")
                .contains("\\u003E")
                .contains("\\u0026")
                .contains("\\u0027")
                .doesNotContain("<script>");
    }

    @Test
    void normalJsonSerializationStillWorks() throws Exception {
        String json = objectMapper.writeValueAsString(Map.of("name", "plain text"));

        assertThat(json).contains("\"name\":\"plain text\"");
    }
}