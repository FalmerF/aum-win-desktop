package ru.ilug.aumwindesktop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.ilug.aumwindesktop.data.model.ApplicationInfo;
import ru.ilug.aumwindesktop.util.WindowsApplicationUtil;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApplicationMonitorService {

    private final ApplicationTimeFrameService applicationTimeFrameService;

    @Scheduled(fixedRate = 1000)
    public void run() {
        ApplicationInfo applicationInfo = WindowsApplicationUtil.getFocusedApplicationInfo();
        applicationTimeFrameService.updateTime(applicationInfo);
    }

}
