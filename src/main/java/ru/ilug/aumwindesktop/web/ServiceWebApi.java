package ru.ilug.aumwindesktop.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ilug.aumwindesktop.data.model.ApplicationTimeFrame;

import java.util.List;

@Component
public class ServiceWebApi {

    private final WebClient webClient;

    public ServiceWebApi(@Value("${application.service.url}") String serviceUrl) {
        webClient = WebClient.builder()
                .baseUrl(serviceUrl)
                .build();
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
}
