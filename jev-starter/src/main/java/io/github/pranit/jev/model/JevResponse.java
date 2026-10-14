package io.github.pranit.jev.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.pranit.jev.api.Answer;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Named answers together with the actual model and token usage returned by Jev.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JevResponse(String model, Map<String, Answer> answers, Usage usage) {

    public JevResponse {
        Assert.hasText(model, "Response must include a model");
        Assert.notEmpty(answers, "Response must include answers");
        Assert.notNull(usage, "Response must include usage");
        answers = Map.copyOf(answers);
    }

    public Answer.Result result(String name) {
        return answer(name, Answer.Result.class);
    }

    public Answer.Choice choice(String name) {
        return answer(name, Answer.Choice.class);
    }

    public Answer.Score score(String name) {
        return answer(name, Answer.Score.class);
    }

    private <T extends Answer> T answer(String name, Class<T> type) {
        var answer = answers.get(name);
        Assert.notNull(answer, "No answer named '" + name + "'");
        Assert.isTrue(type.isInstance(answer), "Answer '" + name + "' is not a " + type.getSimpleName());
        return type.cast(answer);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(
            @JsonProperty("input_tokens") Long inputTokens,
            @JsonProperty("output_tokens") Long outputTokens
    ) {
        public Usage {
            Assert.isTrue(inputTokens != null && inputTokens >= 0, "Input tokens must be a nonnegative number");
            Assert.isTrue(outputTokens != null && outputTokens >= 0, "Output tokens must be a nonnegative number");
        }
    }
}
