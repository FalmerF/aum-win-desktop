package ru.ilug.aumwindesktop.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ApplicationTimeFrame {

    @Id
    @GeneratedValue
    private long id;
    private String exePath;
    private String windowsClass;
    private long startTime;
    private long endTime;
    private boolean active;

}
