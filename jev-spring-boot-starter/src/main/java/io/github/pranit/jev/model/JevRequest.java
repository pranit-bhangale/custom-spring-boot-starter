package io.github.pranit.model;

import io.github.pranit.api.Question;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * State and named questions, with an optional per-request model override.
 */
public record JevRequest(Object state, Map<String, ? extends Question> questions, String model) {

    public JevRequest(Object state, Map<String, ? extends Question> questions) {
        this(state, questions, null);
    }

    public JevRequest {
        Assert.notEmpty(questions, "At least one question is required");
        questions.forEach((name, question) -> {
            Assert.hasText(name, "Question names must not be blank");
            Assert.notNull(question, "Questions must not be null");
        });
        Assert.isTrue(model == null || !model.isBlank(), "Model override must not be blank");
        questions = Collections.unmodifiableMap(new LinkedHashMap<>(questions));
    }
}
