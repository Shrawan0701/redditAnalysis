package com.reddit.analysis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class RedditAuthService {

    private final WebClient webClient = WebClient.create();
    private String accessToken;
    private long tokenExpiry;

    @Value("${reddit.client-id}")
    private String clientId;

    @Value("${reddit.client-secret}")
    private String clientSecret;

    @Value("${reddit.username}")
    private String username;

    @Value("${reddit.password}")
    private String password;

    public synchronized String getAccessToken() {
        long now = System.currentTimeMillis();
        if (accessToken == null || now >= tokenExpiry) {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("grant_type", "password");
            formData.add("username", username);
            formData.add("password", password);

            var response = webClient.post()
                    .uri("https://www.reddit.com/api/v1/access_token")
                    .headers(headers -> headers.setBasicAuth(clientId, clientSecret))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();


            if (response != null) {
                accessToken = response.access_token;
                tokenExpiry = now + (response.expires_in - 60) * 1000L; // renew 1 min early
            }
        }
        return accessToken;
    }

    private static class TokenResponse {
        public String access_token;
        public String token_type;
        public int expires_in;
        public String scope;
    }
}
