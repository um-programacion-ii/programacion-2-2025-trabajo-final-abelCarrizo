package com.abel.eventos.infrastructure.config;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Value("${catedra.api.token}")
    private String catedraToken;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(30000);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.setInterceptors(Collections.singletonList(new JwtInterceptor()));

        return restTemplate;
    }

    private class JwtInterceptor implements ClientHttpRequestInterceptor {
        @Override
        @Nonnull
        public ClientHttpResponse intercept(
                @Nonnull HttpRequest request,
                @Nonnull byte[] body,
                @Nonnull ClientHttpRequestExecution execution) throws IOException {
            request.getHeaders().setBearerAuth(catedraToken);
            return execution.execute(request, body);
        }
    }
}