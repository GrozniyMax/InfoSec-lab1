package com.grozniy.lab1.config;

import org.springframework.boot.jackson.autoconfigure.JsonFactoryBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.SerializableString;
import tools.jackson.core.io.CharacterEscapes;
import tools.jackson.core.io.SerializedString;

/**
 * Configures the application {@code ObjectMapper} to escape HTML-sensitive
 * characters ({@code < > & " '}) in JSON string values, so that
 * user-controlled data returned by the API cannot be interpreted as
 * HTML/script if embedded into a page.
 * <p>
 * Uses Jackson 3 ({@code tools.jackson.*}) and Spring Boot 4's
 * {@link JsonFactoryBuilderCustomizer}, which applies the escapes to the
 * shared {@code JsonFactory} backing the application {@code ObjectMapper}.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public JsonFactoryBuilderCustomizer htmlEscapingCustomizer() {
        return builder -> builder.characterEscapes(new HtmlCharacterEscapes());
    }

    static class HtmlCharacterEscapes extends CharacterEscapes {

        private final int[] asciiEscapes;

        HtmlCharacterEscapes() {
            asciiEscapes = CharacterEscapes.standardAsciiEscapesForJSON();
            asciiEscapes['<'] = CharacterEscapes.ESCAPE_STANDARD;
            asciiEscapes['>'] = CharacterEscapes.ESCAPE_STANDARD;
            asciiEscapes['&'] = CharacterEscapes.ESCAPE_STANDARD;
            asciiEscapes['"'] = CharacterEscapes.ESCAPE_STANDARD;
            asciiEscapes['\''] = CharacterEscapes.ESCAPE_STANDARD;
        }

        @Override
        public int[] getEscapeCodesForAscii() {
            return asciiEscapes;
        }

        @Override
        public SerializableString getEscapeSequence(int ch) {
            switch (ch) {
                case '<':
                    return new SerializedString("\\u003C");
                case '>':
                    return new SerializedString("\\u003E");
                case '&':
                    return new SerializedString("\\u0026");
                case '"':
                    return new SerializedString("\\u0022");
                case '\'':
                    return new SerializedString("\\u0027");
                default:
                    return null;
            }
        }
    }
}