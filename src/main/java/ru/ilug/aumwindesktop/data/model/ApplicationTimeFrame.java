package ru.ilug.aumwindesktop.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ApplicationTimeFrame {

    @Id
    private String exePath;
    private String windowsClass;
    private long startTime;
    private long endTime;
    private boolean active;

}
