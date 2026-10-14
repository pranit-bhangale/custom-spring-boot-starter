package io.github.pranit.jev.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Only the fields belonging to the returned judgment are available on each answer.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Answer.Result.class, name = "result"),
        @JsonSubTypes.Type(value = Answer.Choice.class, name = "choice"),
        @JsonSubTypes.Type(value = Answer.Score.class, name = "score")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public sealed interface Answer permits Answer.Result, Answer.Choice, Answer.Score {

    private static void probability(Double value, String name) {
        Assert.isTrue(value != null && Double.isFinite(value) && value >= 0 && value <= 1,
                "Answer " + name + " must be a number between 0 and 1");
    }

    private static Map<String, Double> probabilities(Map<String, Double> values) {
        Assert.notEmpty(values, "Answer must include probabilities");
        values.forEach((key, value) -> probability(value, "probability"));
        return Map.copyOf(values);
    }

    /**
     * Probability of yes. Applications decide their own threshold.
     */
    record Result(Double result) implements Answer {
        public Result {
            probability(result, "result");
        }
    }

    /**
     * Selected label, reported confidence, and probabilities for the available labels.
     */
    record Choice(String choice, Double confidence, Map<String, Double> probabilities) implements Answer {
        public Choice {
            Assert.hasText(choice, "Choice answer must include a label");
            probability(confidence, "confidence");
            probabilities = Answer.probabilities(probabilities);
            Assert.isTrue(probabilities.containsKey(choice), "Choice label must appear in probabilities");
        }
    }

    /**
     * Weighted zero-based level index; the score can fall between levels.
     */
    record Score(Double score, Double confidence, Map<String, Double> probabilities,
                 Map<String, Object> legend) implements Answer {
        public Score {
            Assert.isTrue(score != null && Double.isFinite(score), "Score answer must include a finite score");
            probability(confidence, "confidence");
            probabilities = Answer.probabilities(probabilities);
            Assert.notEmpty(legend, "Score answer must include a legend");
            legend = Collections.unmodifiableMap(new LinkedHashMap<>(legend));
        }
    }
}
