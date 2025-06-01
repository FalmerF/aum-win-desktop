package ru.ilug.aumwindesktop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ilug.aumwindesktop.data.model.ApplicationInfo;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.event.AuthStatusUpdateEvent;
import ru.ilug.aumwindesktop.util.WindowsApplicationUtil;
import ru.ilug.aumwindesktop.web.ServiceWebApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApplicationMonitorService {

    private final ApplicationTimeFrameService applicationTimeFrameService;
    private final UserService userService;
    private final ServiceWebApi serviceWebApi;
    private final UIService uiService;

    private List<ApplicationStatistic> statistics = Collections.emptyList();

    @EventListener(AuthStatusUpdateEvent.class)
    @Order(1)
    public void onAuthStatusUpdate() {
        updateStatistics();
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    public void tick() {
        ApplicationInfo applicationInfo = WindowsApplicationUtil.getFocusedApplicationInfo();
        if (applicationInfo != null) {
            applicationTimeFrameService.updateTime(applicationInfo);
        }
    }

    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS)
    public void postFrames() {
        if (userService.isAuthorized()) {
            applicationTimeFrameService.postFrames();
            serviceWebApi.getStatistics()
                    .collectList()
                    .subscribe(s -> statistics = s);
        } else {
            statistics = Collections.emptyList();
        }
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.SECONDS)
    public void updateStatistics() {
        if (!uiService.isShowing()) {
            return;
        }

        List<ApplicationStatistic> statistics = applicationTimeFrameService.addLocalStatistics(this.statistics);
        uiService.updateStatisticsTable(statistics);
    }

}
