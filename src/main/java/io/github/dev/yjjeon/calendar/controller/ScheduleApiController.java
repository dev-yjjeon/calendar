package io.github.dev.yjjeon.calendar.controller;

import io.github.dev.yjjeon.calendar.config.auth.CustomUserDetails;
import io.github.dev.yjjeon.calendar.mapper.ScheduleMapper;
import io.github.dev.yjjeon.calendar.model.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleApiController {

    private final ScheduleMapper scheduleMapper;

    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(scheduleMapper.findAllByUserId(userDetails.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Schedule schedule = scheduleMapper.findByIdAndUserId(id, userDetails.getId());
        if (schedule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestBody Schedule schedule, @AuthenticationPrincipal CustomUserDetails userDetails) {
        schedule.setUserId(userDetails.getId());
        scheduleMapper.insert(schedule);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule, @AuthenticationPrincipal CustomUserDetails userDetails) {
        schedule.setId(id);
        schedule.setUserId(userDetails.getId());
        int updated = scheduleMapper.update(schedule);
        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(schedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        int deleted = scheduleMapper.deleteByIdAndUserId(id, userDetails.getId());
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
