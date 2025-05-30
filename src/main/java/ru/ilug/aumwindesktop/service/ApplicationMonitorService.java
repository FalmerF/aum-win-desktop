package ru.ilug.aumwindesktop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ilug.aumwindesktop.data.model.ApplicationInfo;
import ru.ilug.aumwindesktop.util.WindowsApplicationUtil;
import ru.ilug.aumwindesktop.web.ServiceWebApi;

import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApplicationMonitorService {

    private final ApplicationTimeFrameService applicationTimeFrameService;
    private final UserService userService;
    private final ServiceWebApi serviceWebApi;
    private final UIService uiService;

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
        }
    }

    @Scheduled(fixedRate = 15, initialDelay = 5, timeUnit = TimeUnit.SECONDS)
    public void updateStatistics() {
        if (userService.isAuthorized() && uiService.isShowing()) {
            serviceWebApi.getStatistics()
                    .collectList()
                    .subscribe(uiService::updateStatisticsTable);
        }
    }

}
