package io.github.pranit.autoconfigure;

import io.github.pranit.client.JevClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.restclient.autoconfigure.RestClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

/**
 * Registers a JevClient using Spring Boot's configured RestClient.Builder.
 */
@AutoConfiguration(after = RestClientAutoConfiguration.class)
@ConditionalOnClass(RestClient.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "jev", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JevProperties.class)
public final class JevAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JevClient.class)
    JevClient jevClient(RestClient.Builder builder, JevProperties properties, Environment environment) {
        String apiKey = properties.apiKey();
        if (apiKey == null) apiKey = environment.getProperty("TYPESAFE_API_KEY");
        Assert.hasText(apiKey, "Configure jev.api-key or set TYPESAFE_API_KEY to use Jev");
        Assert.hasText(properties.model(), "jev.model must not be blank");
        var baseUrl = properties.baseUrl();
        Assert.isTrue(baseUrl != null && baseUrl.getHost() != null
                        && ("https".equalsIgnoreCase(baseUrl.getScheme()) || "http".equalsIgnoreCase(baseUrl.getScheme()))
                        && baseUrl.getUserInfo() == null && baseUrl.getQuery() == null && baseUrl.getFragment() == null,
                "jev.base-url must be an absolute HTTP(S) URL without credentials, query, or fragment");
        String token = apiKey.trim();
        var restClient = builder.clone()
                .baseUrl(baseUrl.toString().replaceAll("/+$", ""))
                .defaultHeaders(headers -> headers.setBearerAuth(token))
                .build();
        return new JevClient(restClient, properties.model());
    }
}