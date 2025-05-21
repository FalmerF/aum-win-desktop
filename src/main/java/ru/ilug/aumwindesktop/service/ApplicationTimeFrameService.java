package ru.ilug.aumwindesktop.service;

import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.ilug.aumwindesktop.data.model.ApplicationInfo;
import ru.ilug.aumwindesktop.data.model.ApplicationTimeFrame;
import ru.ilug.aumwindesktop.data.repository.ApplicationTimeFrameRepository;

import java.time.Instant;
import java.util.Arrays;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApplicationTimeFrameService {

    private final ApplicationTimeFrameRepository repository;

    @Transactional
    public void updateTime(ApplicationInfo applicationInfo) {
        ApplicationTimeFrame activeTimeFrame = repository.getApplicationTimeFrameByActive(true);

        if (isTimeFrameActual(activeTimeFrame) && activeTimeFrame.getExePath().equals(applicationInfo.getExePath())) {
            extendTimeFrame(activeTimeFrame);
        } else {
            startTimeFrame(activeTimeFrame, applicationInfo);
        }
    }

    private boolean isTimeFrameActual(ApplicationTimeFrame timeFrame) {
        return timeFrame != null && (Instant.now().toEpochMilli() - timeFrame.getEndTime()) < 10000;
    }

    private void extendTimeFrame(ApplicationTimeFrame timeFrame) {
        timeFrame.setEndTime(Instant.now().toEpochMilli());
        repository.save(timeFrame);

        log.info("Application {} opened {} ms", timeFrame.getExePath(), (timeFrame.getEndTime() - timeFrame.getStartTime()));
    }

    private void startTimeFrame(@Nullable ApplicationTimeFrame prevTimeFrame, ApplicationInfo applicationInfo) {
        long timeMillis = Instant.now().toEpochMilli();

        ApplicationTimeFrame timeFrame = ApplicationTimeFrame.builder()
                .exePath(applicationInfo.getExePath())
                .windowsClass(applicationInfo.getWindowsClass())
                .startTime(timeMillis)
                .endTime(timeMillis)
                .active(true)
                .build();

        if (prevTimeFrame != null) {
            prevTimeFrame.setEndTime(timeMillis)
                    .setActive(false);

            repository.saveAll(Arrays.asList(timeFrame, prevTimeFrame));
        } else {
            repository.save(timeFrame);
        }
    }
}
