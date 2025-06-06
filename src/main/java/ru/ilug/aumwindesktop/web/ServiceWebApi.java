package ru.ilug.aumwindesktop.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.data.model.ApplicationTimeFrame;
import ru.ilug.aumwindesktop.event.application.AuthStatusUpdateEvent;
import ru.ilug.aumwindesktop.service.UserService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceWebApi {

    private final UserService userService;

    private WebClient webClient;

    @Value("${application.service.url}")
    private String serviceUrl;

    @EventListener
    @Order(0)
    public void onAuthStatusUpdate(AuthStatusUpdateEvent event) {
        if (event.isAuthorized()) {
            webClient = WebClient.builder()
                    .baseUrl(serviceUrl)
                    .defaultHeaders(headers -> headers.setBearerAuth(event.getToken()))
                    .defaultStatusHandler(code -> code == HttpStatus.UNAUTHORIZED, clientResponse -> {
                        userService.logout();
                        return Mono.error(new RuntimeException("Unauthorized exception, token is invalid, logout"));
                    })
                    .build();
        } else {
            webClient = null;
        }
    }

    public void postTimeFrames(List<ApplicationTimeFrame> frames) {
        webClient.post()
                .uri("/timeframe/post")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(frames)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public Flux<ApplicationStatistic> getStatistics() {
        return webClient.get()
                .uri("/timeframe/statistics")
                .retrieve()
                .bodyToFlux(ApplicationStatistic.class);
    }
}
