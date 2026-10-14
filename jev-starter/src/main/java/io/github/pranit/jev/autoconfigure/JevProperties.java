package io.github.pranit.jev.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.net.URI;

/**
 * Configuration for the blocking Jev client.
 *
 * @param enabled Whether to autoconfigure the Jev client.
 * @param apiKey  API key for TypeSafe. Falls back to TYPESAFE_API_KEY when not configured.
 * @param baseUrl API root URL, without the /v1/systemone endpoint.
 * @param model   Default model used when an evaluation does not specify one.
 */
@ConfigurationProperties("jev")
public record JevProperties(
        @DefaultValue("true") boolean enabled,
        String apiKey,
        @DefaultValue("https://api.typesafe.ai") URI baseUrl,
        @DefaultValue("jev-latest") String model) {
}