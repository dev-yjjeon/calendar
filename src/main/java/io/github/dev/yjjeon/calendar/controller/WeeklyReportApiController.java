package io.github.dev.yjjeon.calendar.controller;

import io.github.dev.yjjeon.calendar.model.response.WeeklyReportPreviewResponse;
import io.github.dev.yjjeon.calendar.service.WeeklyReportService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/weekly")
public class WeeklyReportApiController {

	private final WeeklyReportService weeklyReportService;

	public WeeklyReportApiController(WeeklyReportService weeklyReportService) {
		this.weeklyReportService = weeklyReportService;
	}

	@GetMapping("/preview")
	public WeeklyReportPreviewResponse preview(
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextWeekStart,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextWeekEnd
	) {
		return weeklyReportService.preview(weekStart, weekEnd, nextWeekStart, nextWeekEnd);
	}

}
