package io.github.pranit.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A typed judgment about state. Instructions and descriptions may be text or structured JSON values. */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Question.Result.class, name = "result    "),
        @JsonSubTypes.Type(value = Question.Choice.class, name = "choice"),
        @JsonSubTypes.Type(value = Question.Score.class, name = "score")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface Question permits Question.Result, Question.Choice, Question.Score {

    static Result result(Object instructions) {
        return new Result(instructions, null);
    }

    /** Describe the yes and no outcomes explicitly. */
    static Result result(Object instructions, Object whenTrue, Object whenFalse) {
        var criteria = new LinkedHashMap<String, Object>();
        criteria.put("true", whenTrue);
        criteria.put("false", whenFalse);
        return new Result(instructions, criteria);
    }

    static Choice choice(Object instructions, Map<String, ?> options) {
        return new Choice(instructions, options);
    }

    /** Create undescribed options without needing a map containing null values. */
    static Choice choice(Object instructions, String... labels) {
        Assert.notNull(labels, "Choice labels must not be null");
        var criteria = new LinkedHashMap<String, Object>();
        for (String label : labels) {
            Assert.isTrue(!criteria.containsKey(label), "Choice labels must be unique");
            criteria.put(label, null);
        }
        return new Choice(instructions, criteria);
    }

    static Score score(Object instructions, List<?> levels) {
        return new Score(instructions, levels);
    }

    static Score score(Object instructions, String... levels) {
        Assert.notNull(levels, "Score levels must not be null");
        return new Score(instructions, Arrays.asList(levels));
    }

    /** Yes/no judgment. Criteria keys, when supplied, are "true" and "false". */
    record Result(Object instructions,
                @JsonInclude(value = JsonInclude.Include.NON_NULL, content = JsonInclude.Include.ALWAYS)
                Map<String, ?> criteria) implements Question {
        public Result {
            if (criteria != null) {
                Assert.isTrue(criteria.keySet()
                                .stream()
                                .allMatch(key -> "true".equals(key) || "false".equals(key)),
                        "Result criteria keys must be true or false");
                criteria = Collections.unmodifiableMap(new LinkedHashMap<>(criteria));
            }
        }
    }

    /** Choose a named option. Null descriptions mean that the label alone describes the option. */
    record Choice(Object instructions,
                  @JsonInclude(content = JsonInclude.Include.ALWAYS) Map<String, ?> criteria) implements Question {
        public Choice {
            Assert.notEmpty(criteria, "At least one choice option is required");
            criteria.keySet().forEach(key -> Assert.hasText(key, "Choice labels must not be blank"));
            criteria = Collections.unmodifiableMap(new LinkedHashMap<>(criteria));
        }
    }

    /** Ordered rubric with at least two levels, indexed from zero. */
    record Score(Object instructions, List<?> criteria) implements Question {
        public Score {
            Assert.notNull(criteria, "Score levels must not be null");
            Assert.isTrue(criteria.size() >= 2, "At least two score levels are required");
            // List.copyOf rejects null descriptions, which the API permits.
            criteria = Collections.unmodifiableList(new ArrayList<>(criteria));
        }
    }
}