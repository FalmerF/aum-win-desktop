package ru.ilug.aumwindesktop.service;

import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ilug.aumwindesktop.data.model.AccessTokenRequest;
import ru.ilug.aumwindesktop.data.model.AccessTokenResponse;
import ru.ilug.aumwindesktop.data.model.AuthCodeCallback;
import ru.ilug.aumwindesktop.web.AuthCodeCallbackServer;

@Slf4j
@Lazy
@Service
public class AuthorizationFlowService {

    private final AuthCodeCallbackServer callbackServer;
    private final UserService userService;
    private final UIService uiService;

    private final WebClient githubAuthClient;

    @Value("${application.auth.github.client-id}")
    private String clientId;
    @Value("${application.auth.github.client-secret}")
    private String clientSecret;

    private boolean active;

    public AuthorizationFlowService(AuthCodeCallbackServer callbackServer, UserService userService, UIService uiService) {
        this.callbackServer = callbackServer;
        this.userService = userService;
        this.uiService = uiService;

        githubAuthClient = WebClient.builder()
                .baseUrl("https://github.com/login/oauth")
                .build();
    }

    public void start() {
        if (active) {
            return;
        }
        active = true;

        new Thread(() -> {
            try {
                AuthCodeCallback codeCallback = callbackServer.start();
                AccessTokenResponse response = exchangeCodeForToken(codeCallback);
                userService.setToken(response.getAccessToken());
            } catch (Exception e) {
                log.error("Error on authorization", e);
            } finally {
                Platform.runLater(uiService::updateHeaderState);
                active = false;
                callbackServer.stop();
            }
        }).start();
    }

    private AccessTokenResponse exchangeCodeForToken(AuthCodeCallback codeCallback) {
        return githubAuthClient.post()
                .uri("/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(
                        AccessTokenRequest.builder()
                                .clientId(clientId)
                                .clientSecret(clientSecret)
                                .code(codeCallback.getCode())
                                .redirectUri(codeCallback.getRedirectUri())
                                .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AccessTokenResponse.class)
                .block();
    }

}
