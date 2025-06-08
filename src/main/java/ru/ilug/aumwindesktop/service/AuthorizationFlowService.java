package ru.ilug.aumwindesktop.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ilug.aumwindesktop.data.model.AccessTokenResponse;
import ru.ilug.aumwindesktop.data.model.AuthCodeCallback;
import ru.ilug.aumwindesktop.util.PkceUtil;
import ru.ilug.aumwindesktop.web.AuthCodeCallbackServer;

@Slf4j
@Lazy
@Service
public class AuthorizationFlowService {

    private final AuthCodeCallbackServer callbackServer;
    private final UserService userService;

    private final WebClient authServerClient;

    @Value("${application.auth.client-id}")
    private String clientId;

    private boolean active;

    public AuthorizationFlowService(AuthCodeCallbackServer callbackServer, UserService userService,
                                    @Value("${application.auth.url}") String authUrl) {
        this.callbackServer = callbackServer;
        this.userService = userService;

        authServerClient = WebClient.builder()
                .baseUrl(authUrl)
                .build();
    }

    public void start() {
        if (active) {
            return;
        }
        active = true;

        new Thread(() -> {
            try {
                String codeVerifier = PkceUtil.generateCodeVerifier();
                String codeChallenge = PkceUtil.generateCodeChallenge(codeVerifier);

                AuthCodeCallback codeCallback = callbackServer.start(codeChallenge);
                AccessTokenResponse response = exchangeCodeForToken(codeCallback, codeVerifier);
                userService.updateToken(response.getAccessToken());
            } catch (Exception e) {
                log.error("Error on authorization", e);
                userService.logout();
            } finally {
                active = false;
                callbackServer.stop();
            }
        }).start();
    }

    private AccessTokenResponse exchangeCodeForToken(AuthCodeCallback codeCallback, String codeVerifier) {
        return authServerClient.post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_JSON)
                .body(
                        BodyInserters.fromFormData("client_id", clientId)
                                .with("code", codeCallback.getCode())
                                .with("redirect_uri", codeCallback.getRedirectUri())
                                .with("grant_type", "authorization_code")
                                .with("code_verifier", codeVerifier)
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(AccessTokenResponse.class)
                .block();
    }

}
