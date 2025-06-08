package ru.ilug.aumwindesktop.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.sun.jna.platform.win32.Crypt32Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
import java.text.ParseException;

@Slf4j
@Service
public class UserService {

    private static final Path TOKEN_PATH = Path.of("./token");

    private final ApplicationEventPublisher eventPublisher;

    private final WebClient authServerClient;

    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    private String token;
    @Getter
    private User user;

    public UserService(ApplicationEventPublisher eventPublisher, @Value("${application.auth.url}") String authUrl) {
        this.eventPublisher = eventPublisher;

        this.authServerClient = WebClient.builder()
                .baseUrl(authUrl)
                .defaultStatusHandler(code -> code == HttpStatus.UNAUTHORIZED, clientResponse -> {
                    logout();
                    return Mono.error(new RuntimeException("Unauthorized exception, token is invalid, logout"));
                })
                .build();
    }

    public void onApplicationLaunched() {
        validateAndSetToken(loadTokenFromSecureFile());
    }

    public void updateToken(String token) {
        validateAndSetToken(token);
        try {
            saveTokenToSecureFile();
        } catch (Exception e) {
            log.error("Error on save access token");
        }
    }

    private void validateAndSetToken(String token) {
        try {
            user = validateTokenAndGetUser(token);
            setToken(token);
        } catch (Exception e) {
            invalidateToken();
            log.error("Error on validate token", e);
        }
    }

    private void setToken(String token) {
        this.token = token;
        AuthStatusUpdateEvent event = new AuthStatusUpdateEvent(this, user, token, isAuthorized());
        eventPublisher.publishEvent(event);
    }

    private void invalidateToken() {
        if (Files.exists(TOKEN_PATH)) {
            try {
                Files.delete(TOKEN_PATH);
            } catch (Exception ignore) {
            }
        }

        this.user = null;
        setToken(null);
    }

    private User validateTokenAndGetUser(String token) throws ParseException, BadJOSEException, JOSEException {
        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = getJwtProcessor();

        JWTClaimsSet claimsSet = jwtProcessor.process(token, null);
        String name = claimsSet.getStringClaim("name");
        String picture = claimsSet.getStringClaim("picture");

        return new User(name, picture);
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

    public void logout() {
        invalidateToken();
    }

    private ConfigurableJWTProcessor<SecurityContext> getJwtProcessor() throws ParseException {
        if (jwtProcessor == null) {
            jwtProcessor = createJwtProcessor();
        }

        return jwtProcessor;
    }

    private ConfigurableJWTProcessor<SecurityContext> createJwtProcessor() throws ParseException {
        String jwkSetJson = getJWKS();

        JWKSet jwkSet = JWKSet.parse(jwkSetJson);
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(jwkSet);

        ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
        JWSKeySelector<SecurityContext> keySelector =
                new JWSVerificationKeySelector<>(expectedJWSAlg, jwkSource);
        jwtProcessor.setJWSKeySelector(keySelector);

        return jwtProcessor;
    }

    private String getJWKS() {
        return authServerClient.get()
                .uri("/oauth2/jwks")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
