package io.github.dev.yjjeon.calendar.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String name;
    private LocalDateTime createdAt;
}
