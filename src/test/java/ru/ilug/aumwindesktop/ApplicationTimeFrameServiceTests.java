package ru.ilug.aumwindesktop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.ilug.aumwindesktop.data.model.ApplicationInfo;
import ru.ilug.aumwindesktop.data.model.ApplicationTimeFrame;
import ru.ilug.aumwindesktop.data.repository.ApplicationTimeFrameRepository;
import ru.ilug.aumwindesktop.service.ApplicationTimeFrameService;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ApplicationTimeFrameServiceTests {

    @Autowired
    private ApplicationTimeFrameService applicationTimeFrameService;
    @Autowired
    private ApplicationTimeFrameRepository repository;

    @Test
    public void testAddFirstActiveFrame() {
        ApplicationInfo applicationInfo = new ApplicationInfo("test.exe", "test.class");
        applicationTimeFrameService.updateTime(applicationInfo);

        assertEquals(1, repository.count());
        ApplicationTimeFrame frame = repository.findAll().get(0);

        assertEquals("test.exe", frame.getExePath());
        assertEquals("test.class", frame.getWindowsClass());
        assertTrue(frame.isActive());
        assertEquals(frame.getEndTime(), frame.getStartTime());
    }

    @Test
    public void testMergeTimeFramesWith1SecondDifferent() {
        long time = Instant.now().toEpochMilli();
        ApplicationTimeFrame firstFrame = new ApplicationTimeFrame(0, "test.exe", "test.class", time - 1000, time, true);
        repository.save(firstFrame);

        ApplicationInfo applicationInfo = new ApplicationInfo("test.exe", "test.class");
        applicationTimeFrameService.updateTime(applicationInfo);

        assertEquals(1, repository.count());

        ApplicationTimeFrame frame = repository.findAll().get(0);

        assertEquals("test.exe", frame.getExePath());
        assertEquals("test.class", frame.getWindowsClass());
        assertTrue(frame.isActive());
        assertEquals(time - 1000, frame.getStartTime());
    }

    @Test
    public void testMergeNonActiveTimeFrames() {
        long time = Instant.now().toEpochMilli() - 5000;
        ApplicationTimeFrame firstFrame = new ApplicationTimeFrame(0, "test.exe", "test.class", time, time, false);
        repository.save(firstFrame);

        ApplicationInfo applicationInfo = new ApplicationInfo("test.exe", "test.class");
        applicationTimeFrameService.updateTime(applicationInfo);

        assertEquals(2, repository.count());
    }

    @Test
    public void testMergeDeprecatedActiveTimeFrame() {
        long time = Instant.now().toEpochMilli() - 15000;
        ApplicationTimeFrame firstFrame = new ApplicationTimeFrame(0, "test.exe", "test.class", time, time, true);
        repository.save(firstFrame);

        ApplicationInfo applicationInfo = new ApplicationInfo("test.exe", "test.class");
        applicationTimeFrameService.updateTime(applicationInfo);

        assertEquals(2, repository.count());
    }

    @Test
    public void testMergeTimeFramesWithDifferentPath() {
        long time = Instant.now().toEpochMilli();
        ApplicationTimeFrame firstFrame = new ApplicationTimeFrame(0, "test1.exe", "test1.class", time - 1000, time, true);
        repository.save(firstFrame);

        ApplicationInfo applicationInfo = new ApplicationInfo("test2.exe", "test2.class");
        applicationTimeFrameService.updateTime(applicationInfo);

        assertEquals(2, repository.count());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ApplicationTimeFrameService applicationTimeFrameService(ApplicationTimeFrameRepository repository) {
            return new ApplicationTimeFrameService(repository, null);
        }

    }
}
