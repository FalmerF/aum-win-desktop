package ru.ilug.aumwindesktop;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.ilug.aumwindesktop.data.model.ApplicationInfo;
import ru.ilug.aumwindesktop.data.model.ApplicationStatistic;
import ru.ilug.aumwindesktop.data.model.ApplicationTimeFrame;
import ru.ilug.aumwindesktop.data.repository.ApplicationTimeFrameRepository;
import ru.ilug.aumwindesktop.service.ApplicationTimeFrameService;
import ru.ilug.aumwindesktop.web.ServiceWebApi;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ApplicationTimeFrameServiceTests {

    @Autowired
    private ApplicationTimeFrameService applicationTimeFrameService;
    @Autowired
    private ApplicationTimeFrameRepository repository;
    @Autowired
    private ServiceWebApi serviceWebApi;

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

    @Test
    public void testPostWithoutActiveTimeFrames() {
        Mockito.doNothing().when(serviceWebApi).postTimeFrames(Mockito.anyList());

        ApplicationTimeFrame firstFrame = new ApplicationTimeFrame(0, "test1.exe", "test1.class", 0, 0, false);
        ApplicationTimeFrame secondFrame = new ApplicationTimeFrame(0, "test2.exe", "test2.class", 0, 0, false);
        repository.saveAll(List.of(firstFrame, secondFrame));

        applicationTimeFrameService.postFrames();

        assertEquals(0, repository.count());
    }

    @Test
    public void testPostWithActiveTimeFrames() {
        Mockito.doNothing().when(serviceWebApi).postTimeFrames(Mockito.anyList());

        ApplicationTimeFrame firstFrame = new ApplicationTimeFrame(0, "test1.exe", "test1.class", 0, 0, false);
        ApplicationTimeFrame secondFrame = new ApplicationTimeFrame(0, "test2.exe", "test2.class", 0, 0, true);
        repository.saveAll(List.of(firstFrame, secondFrame));

        applicationTimeFrameService.postFrames();

        assertEquals(1, repository.count());
    }

    @Test
    public void testPostTimeFramesWithException() {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
                .when(serviceWebApi).postTimeFrames(Mockito.anyList());

        ApplicationTimeFrame firstFrame = new ApplicationTimeFrame(0, "test1.exe", "test1.class", 0, 0, false);
        ApplicationTimeFrame secondFrame = new ApplicationTimeFrame(0, "test2.exe", "test2.class", 0, 0, true);
        repository.saveAll(List.of(firstFrame, secondFrame));

        assertThrows(ResponseStatusException.class, () -> applicationTimeFrameService.postFrames());

        assertEquals(2, repository.count());
    }

    @Test
    public void testAddLocalStatistics() {
        Mockito.doThrow(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR))
                .when(serviceWebApi).postTimeFrames(Mockito.anyList());

        ApplicationTimeFrame frame1 = new ApplicationTimeFrame(0, "test1.exe", "test1.class", 0, 1000, false);
        ApplicationTimeFrame frame2 = new ApplicationTimeFrame(0, "test2.exe", "test2.class", 0, 2000, false);
        ApplicationTimeFrame frame3 = new ApplicationTimeFrame(0, "test3.exe", "test3.class", 0, 3000, false);
        ApplicationTimeFrame frame4 = new ApplicationTimeFrame(0, "test4.exe", "test4.class", 0, 3000, false);
        repository.saveAll(List.of(frame1, frame2, frame3, frame4));

        List<ApplicationStatistic> statistics = List.of(
                new ApplicationStatistic("test1.exe", 2),
                new ApplicationStatistic("test2.exe", 3),
                new ApplicationStatistic("test3.exe", 1)
        );

        Map<String, ApplicationStatistic> result = applicationTimeFrameService.addLocalStatistics(statistics)
                .stream().collect(Collectors.toMap(ApplicationStatistic::getExePath, r -> r));

        assertEquals(4, result.size());
        assertEquals(3, result.get("test1.exe").getSeconds());
        assertEquals(5, result.get("test2.exe").getSeconds());
        assertEquals(4, result.get("test3.exe").getSeconds());
        assertEquals(3, result.get("test4.exe").getSeconds());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        public ServiceWebApi serviceWebApi() {
            return Mockito.mock(ServiceWebApi.class);
        }

        @Bean
        public ApplicationTimeFrameService applicationTimeFrameService(ApplicationTimeFrameRepository repository, ServiceWebApi serviceWebApi) {
            return new ApplicationTimeFrameService(repository, serviceWebApi);
        }

    }
}
