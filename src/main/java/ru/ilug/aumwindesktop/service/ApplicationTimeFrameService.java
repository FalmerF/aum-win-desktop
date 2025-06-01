package ru.ilug.aumwindesktop.service;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import ru.ilug.aumwindesktop.data.model.ApplicationInfo;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.data.model.ApplicationTimeFrame;
import ru.ilug.aumwindesktop.data.repository.ApplicationTimeFrameRepository;
import ru.ilug.aumwindesktop.web.ServiceWebApi;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class ApplicationTimeFrameService {

    private final ApplicationTimeFrameRepository repository;
    private final ServiceWebApi serviceWebApi;

    @Transactional
    public void updateTime(@Nonnull ApplicationInfo applicationInfo) {
        ApplicationTimeFrame activeTimeFrame = repository.getApplicationTimeFrameByActive(true);

        if (isTimeFrameActual(activeTimeFrame) && activeTimeFrame.getExePath().equals(applicationInfo.getExePath())) {
            extendTimeFrame(activeTimeFrame);
        } else {
            startTimeFrame(activeTimeFrame, applicationInfo);
        }
    }

    @Transactional
    public void postFrames() {
        List<ApplicationTimeFrame> timeFrames = repository.findAll();

        serviceWebApi.postTimeFrames(timeFrames);

        ApplicationTimeFrame activeTimeFrame = timeFrames.stream().filter(ApplicationTimeFrame::isActive)
                .findFirst().orElse(null);

        if (activeTimeFrame != null) {
            activeTimeFrame.setStartTime(activeTimeFrame.getEndTime());
            repository.deleteAllByActive(false);
            repository.save(activeTimeFrame);
        } else {
            repository.deleteAll();
        }
    }

    public List<ApplicationStatistic> addLocalStatistics(List<ApplicationStatistic> statisticsList) {
        List<ApplicationTimeFrame> timeFrames = repository.findAll();
        Map<String, ApplicationStatistic> statistics = statisticsList.stream()
                .map(s -> new ApplicationStatistic(s.getExePath(), s.getSeconds()))
                .collect(Collectors.toMap(ApplicationStatistic::getExePath, s -> s));

        for (ApplicationTimeFrame frame : timeFrames) {
            long seconds = (frame.getEndTime() - frame.getStartTime()) / 1000;

            ApplicationStatistic statistic = statistics.computeIfAbsent(frame.getExePath(), key -> new ApplicationStatistic(key, 0));
            statistic.addSeconds(seconds);
        }

        return new ArrayList<>(statistics.values());
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
