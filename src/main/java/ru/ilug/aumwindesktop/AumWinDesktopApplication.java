package ru.ilug.aumwindesktop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AumWinDesktopApplication {

    public static void main(String[] args) {
        SpringApplication.run(AumWinDesktopApplication.class, args);
    }

}
