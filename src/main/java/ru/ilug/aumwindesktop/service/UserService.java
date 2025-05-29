package ru.ilug.aumwindesktop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ilug.aumwindesktop.data.model.User;

@Slf4j
@Service
public class UserService {

    private final WebClient githubClient;

    private String token;

    public UserService() {
        this.githubClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .build();
    }

    public void setToken(String token) {
        this.token = token;
        updateUserInformation();
    }

    private void updateUserInformation() {
        User user = githubClient.get()
                .uri("/user")
                .headers(headers -> headers.setBearerAuth(token))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(User.class)
                .block();

        log.info("User info: {}", user);
    }

}
