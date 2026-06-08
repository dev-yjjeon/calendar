package io.github.dev.yjjeon.calendar.controller;

import io.github.dev.yjjeon.calendar.mapper.ScheduleMapper;
import io.github.dev.yjjeon.calendar.model.Schedule;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleApiController {

    private final ScheduleMapper scheduleMapper;

    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules() {
        return ResponseEntity.ok(scheduleMapper.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Schedule> getScheduleById(@PathVariable Long id) {
        Schedule schedule = scheduleMapper.findById(id);
        if (schedule == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestBody Schedule schedule) {
        scheduleMapper.insert(schedule);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Schedule> updateSchedule(@PathVariable Long id, @RequestBody Schedule schedule) {
        schedule.setId(id);
        int updated = scheduleMapper.update(schedule);
        if (updated == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(schedule);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long id) {
        int deleted = scheduleMapper.delete(id);
        if (deleted == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
