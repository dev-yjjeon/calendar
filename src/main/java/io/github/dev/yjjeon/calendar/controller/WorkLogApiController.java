package io.github.dev.yjjeon.calendar.controller;

import io.github.dev.yjjeon.calendar.model.request.WorkLogRequest;
import io.github.dev.yjjeon.calendar.model.request.WorkLogTimeRequest;
import io.github.dev.yjjeon.calendar.model.response.WorkLogResponse;
import io.github.dev.yjjeon.calendar.service.WorkLogService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/work-logs")
public class WorkLogApiController {

	private final WorkLogService workLogService;

	public WorkLogApiController(WorkLogService workLogService) {
		this.workLogService = workLogService;
	}

	@GetMapping
	public List<WorkLogResponse> findBetween(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
	) {
		return workLogService.findBetween(start, end);
	}

	@GetMapping("/incomplete")
	public List<WorkLogResponse> findIncomplete() {
		return workLogService.findIncomplete();
	}

	@PostMapping
	public WorkLogResponse create(@Valid @RequestBody WorkLogRequest request) {
		return workLogService.create(request);
	}

	@PutMapping("/{id}")
	public WorkLogResponse update(@PathVariable Long id, @Valid @RequestBody WorkLogRequest request) {
		return workLogService.update(id, request);
	}

	@PatchMapping("/{id}/time")
	public WorkLogResponse updateTime(@PathVariable Long id, @Valid @RequestBody WorkLogTimeRequest request) {
		return workLogService.updateTime(id, request);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		workLogService.delete(id);
	}

}
