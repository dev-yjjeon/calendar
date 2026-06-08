package io.github.dev.yjjeon.calendar.controller;

import io.github.dev.yjjeon.calendar.config.auth.CustomUserDetails;
import io.github.dev.yjjeon.calendar.model.request.WeeklyReportRequest;
import io.github.dev.yjjeon.calendar.model.response.WeeklyReportPreviewResponse;
import io.github.dev.yjjeon.calendar.service.WeeklyReportService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nextWeekEnd,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) {
		return weeklyReportService.preview(userDetails.getId(), weekStart, weekEnd, nextWeekStart, nextWeekEnd);
	}

	@PostMapping("/download")
	public void download(
		@RequestBody WeeklyReportRequest request,
		HttpServletResponse response,
		@AuthenticationPrincipal CustomUserDetails userDetails
	) throws IOException {
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		
		String writerName = userDetails.getName();
		String dateStr = request.reportDate() != null ? request.reportDate().toString().replace("-", "") : "";
		String fileName = URLEncoder.encode(dateStr + "_개발팀_주간혁신회의_" + writerName + ".xlsx", StandardCharsets.UTF_8)
			.replaceAll("\\+", "%20");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

		WeeklyReportRequest finalRequest = new WeeklyReportRequest(
			request.reportDate(),
			writerName,
			request.weekStart(),
			request.weekEnd(),
			request.nextWeekStart(),
			request.nextWeekEnd(),
			request.instructions(),
			request.performances(),
			request.progresses(),
			request.delays()
		);

		weeklyReportService.generateExcel(finalRequest, response.getOutputStream());
	}

}
