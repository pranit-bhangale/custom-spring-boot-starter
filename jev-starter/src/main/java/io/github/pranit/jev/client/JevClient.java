package io.github.pranit.jev.client;

import io.github.pranit.jev.api.Answer;
import io.github.pranit.jev.api.Question;
import io.github.pranit.jev.model.JevRequest;
import io.github.pranit.jev.model.JevResponse;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

/**
 * Synchronous Jev client for Spring MVC applications. Reuse one instance across requests.
 * Calls execute on the caller's thread, including virtual MVC request threads when enabled by Spring Boot.
 */
public final class JevClient {

    private final RestClient restClient;
    private final String model;

    /**
     * Create a client using an already configured RestClient (base URL, authentication, and transport).
     * Most applications should inject the autoconfigured JevClient instead.
     */
    public JevClient(RestClient restClient, String model) {
        Assert.notNull(restClient, "RestClient must not be null");
        Assert.hasText(model, "Default model must not be blank");
        this.restClient = restClient;
        this.model = model;
    }

    public JevResponse evaluate(Object state, Map<String, ? extends Question> questions) {
        return evaluate(new JevRequest(state, questions));
    }

    /**
     * Evaluate one batch of questions. Uses Spring's standard RestClient exceptions and makes no automatic retries.
     *
     * @throws IllegalArgumentException if the request is null or invalid
     * @throws RestClientException      if HTTP communication or response decoding/validation fails
     */
    public JevResponse evaluate(JevRequest request) {
        Assert.notNull(request, "Request must not be null");
        var body = new JevRequest(request.state(), request.questions(),
                request.model() == null ? model : request.model());
        var response = restClient.post()
                .uri("/v1/systemone")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JevResponse.class);
        if (response == null) {
            throw new RestClientException("Jev returned an empty response");
        }
        if (!response.answers().keySet().equals(request.questions().keySet())) {
            throw new RestClientException("Jev response answer names do not match the requested questions");
        }
        request.questions().forEach((name, question) -> {
            Answer answer = response.answers().get(name);
            boolean matches = question instanceof Question.Result && answer instanceof Answer.Result
                    || question instanceof Question.Choice && answer instanceof Answer.Choice
                    || question instanceof Question.Score && answer instanceof Answer.Score;
            if (!matches) {
                throw new RestClientException("Jev response answer type does not match question '" + name + "'");
            }
        });
        return response;
    }
}