package io.github.dev.yjjeon.calendar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WeeklyReportController {

	@GetMapping("/reports/weekly")
	public String weeklyReportPage() {
		return "weekly-report";
	}

}
