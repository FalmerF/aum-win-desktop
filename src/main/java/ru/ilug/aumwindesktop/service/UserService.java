package ru.ilug.aumwindesktop.service;

import com.sun.jna.platform.win32.Crypt32Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.ilug.aumwindesktop.data.model.User;
import ru.ilug.aumwindesktop.event.application.AuthStatusUpdateEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Service
public class UserService {

    private static final Path TOKEN_PATH = Path.of("./token");

    private final ApplicationEventPublisher eventPublisher;

    private final WebClient githubClient;

    private String token;
    @Getter
    private User user;

    public UserService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        this.githubClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .defaultStatusHandler(code -> code == HttpStatus.UNAUTHORIZED, clientResponse -> {
                    logout();
                    return Mono.error(new RuntimeException("Unauthorized exception, token is invalid, logout"));
                })
                .build();
    }

    public void onApplicationLaunched() {
        setToken(loadTokenFromSecureFile());
    }

    public void updateToken(String token) {
        setToken(token);
        try {
            saveTokenToSecureFile();
        } catch (Exception e) {
            log.error("Error on save access token");
        }
    }

    public void setToken(String token) {
        this.token = token;
        this.user = null;

        if (token != null) {
            updateUserInformation();
        }

        AuthStatusUpdateEvent event = new AuthStatusUpdateEvent(this, user, token, isAuthorized());
        eventPublisher.publishEvent(event);
    }

    public boolean isAuthorized() {
        return token != null && user != null;
    }

    private void saveTokenToSecureFile() throws IOException {
        byte[] encryptedData = Crypt32Util.cryptProtectData(token.getBytes());

        if (Files.exists(TOKEN_PATH)) {
            Files.delete(TOKEN_PATH);
        }

        Files.write(TOKEN_PATH, encryptedData);
    }

    private String loadTokenFromSecureFile() {
        if (!Files.exists(TOKEN_PATH)) {
            return null;
        }

        try {
            byte[] encryptedData = Files.readAllBytes(TOKEN_PATH);
            byte[] data = Crypt32Util.cryptUnprotectData(encryptedData);
            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateUserInformation() {
        try {
            user = githubClient.get()
                    .uri("/user")
                    .headers(headers -> headers.setBearerAuth(token))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(User.class)
                    .block();
        } catch (Exception e) {
            log.error("Error on get user information", e);
        }
    }

    public void logout() {
        invalidateToken();
    }

    private void invalidateToken() {
        if (Files.exists(TOKEN_PATH)) {
            try {
                Files.delete(TOKEN_PATH);
            } catch (Exception ignore) {
            }
        }

        setToken(null);
    }
}
