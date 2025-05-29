package ru.ilug.aumwindesktop.web;

import javafx.application.Platform;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.data.model.ApplicationTimeFrame;
import ru.ilug.aumwindesktop.event.AuthStatusUpdateEvent;

import java.util.List;

@Component
public class ServiceWebApi {

    private WebClient webClient;

    @Value("${application.service.url}")
    private String serviceUrl;

    @EventListener
    public void onAuthStatusUpdate(AuthStatusUpdateEvent event) {
        if (event.isAuthorized()) {
            webClient = WebClient.builder()
                    .baseUrl(serviceUrl)
                    .defaultHeaders(headers -> headers.setBearerAuth(event.getToken()))
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
