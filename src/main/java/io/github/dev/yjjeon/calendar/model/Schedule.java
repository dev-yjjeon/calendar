package io.github.dev.yjjeon.calendar.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    private Long id;
    private Long userId;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String category;
    private String status;
    private Boolean allDay;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
