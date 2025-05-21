package ru.ilug.aumwindesktop.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ilug.aumwindesktop.data.model.ApplicationTimeFrame;

@Repository
public interface ApplicationTimeFrameRepository extends JpaRepository<ApplicationTimeFrame, String> {

    ApplicationTimeFrame getApplicationTimeFrameByActive(boolean active);

}
