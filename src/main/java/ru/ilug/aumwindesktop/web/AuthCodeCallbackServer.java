package ru.ilug.aumwindesktop.web;

import com.sun.net.httpserver.HttpServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import ru.ilug.aumwindesktop.data.model.AuthCodeCallback;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Lazy
@Component
public class AuthCodeCallbackServer {

    private HttpServer server;

    @Value("${application.auth.client-id}")
    private String clientId;

    public AuthCodeCallback start(String codeChallenge) throws Exception {
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        this.server = createAndStartServer(codeFuture);

        int port = server.getAddress().getPort();
        String redirectUri = String.format("http://127.0.0.1:%s/login/oauth2/code", port);

        URI authUri = buildAuthUri(redirectUri, codeChallenge);

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(authUri);
        } else {
            throw new RuntimeException("Can't open browser");
        }

        String code = codeFuture.get(2, TimeUnit.MINUTES);
        return new AuthCodeCallback(code, redirectUri);
    }

    private HttpServer createAndStartServer(CompletableFuture<String> codeFuture) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

        server.createContext("/login/oauth2/code", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            Map<String, String> parameters = getQueryParameters(query);

            codeFuture.complete(parameters.get("code"));

            String response = "You can close this page and return to the app";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.start();

        return server;
    }

    private URI buildAuthUri(String redirectUri, String codeChallenge) {
        return UriComponentsBuilder
                .fromUriString("http://localhost:9000/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid profile")
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build().toUri();
    }

    public void stop() {
        if (server != null) {
            server.stop(1000);
            server = null;
        }
    }

    private Map<String, String> getQueryParameters(String query) {
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
    }

}
