package io.github.dev.yjjeon.calendar.service;

import io.github.dev.yjjeon.calendar.mapper.WorkLogMapper;
import io.github.dev.yjjeon.calendar.model.entity.WorkLog;
import io.github.dev.yjjeon.calendar.model.response.WeeklyReportPreviewResponse;
import io.github.dev.yjjeon.calendar.model.response.WeeklyReportRow;
import io.github.dev.yjjeon.calendar.model.response.WorkLogResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WeeklyReportService {

	private static final List<String> WORK_TYPES = List.of("프로젝트 일반", "프로젝트 지원협조", "유지보수", "일반업무", "공유");
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("M/d");

	private final WorkLogMapper workLogMapper;

	public WeeklyReportService(WorkLogMapper workLogMapper) {
		this.workLogMapper = workLogMapper;
	}

	@Transactional(readOnly = true)
	public WeeklyReportPreviewResponse preview(LocalDate weekStart, LocalDate weekEnd, LocalDate nextWeekStart, LocalDate nextWeekEnd) {
		List<WorkLog> thisWeekLogs = findReportLogs(weekStart, weekEnd);
		List<WorkLog> nextWeekLogs = findReportLogs(nextWeekStart, nextWeekEnd);
		List<WorkLogResponse> candidates = new ArrayList<>();
		candidates.addAll(thisWeekLogs.stream().map(WorkLogResponse::from).toList());
		candidates.addAll(nextWeekLogs.stream().map(WorkLogResponse::from).toList());

		List<WeeklyReportRow> rows = WORK_TYPES.stream()
			.map(workType -> new WeeklyReportRow(
				workType,
				buildReportText(thisWeekLogs, workType, false),
				buildReportText(nextWeekLogs, workType, true)
			))
			.toList();

		return new WeeklyReportPreviewResponse(candidates, rows);
	}

	private List<WorkLog> findReportLogs(LocalDate start, LocalDate end) {
		return workLogMapper.findBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay())
			.stream()
			.filter(WorkLog::isReportIncluded)
			.toList();
	}

	private String buildReportText(List<WorkLog> workLogs, String workType, boolean nextWeek) {
		Map<String, List<WorkLog>> logsByProject = new LinkedHashMap<>();

		for (WorkLog workLog : workLogs) {
			if (!workType.equals(toWorkType(workLog.getCategory()))) {
				continue;
			}

			String projectName = normalizeProjectName(workLog.getProjectName());
			logsByProject.computeIfAbsent(projectName, key -> new ArrayList<>()).add(workLog);
		}

		List<String> projectBlocks = new ArrayList<>();
		int projectNumber = 1;

		for (Map.Entry<String, List<WorkLog>> entry : logsByProject.entrySet()) {
			List<String> lines = new ArrayList<>();
			lines.add(projectNumber + ". " + entry.getKey());

			for (WorkLog workLog : entry.getValue()) {
				lines.add("- " + formatWorkLogLine(workLog, nextWeek));
			}

			projectBlocks.add(String.join(System.lineSeparator(), lines));
			projectNumber++;
		}

		return String.join(System.lineSeparator() + System.lineSeparator(), projectBlocks);
	}

	private String formatWorkLogLine(WorkLog workLog, boolean nextWeek) {
		StringBuilder line = new StringBuilder(workLog.getTitle());
		String suffix = buildSuffix(workLog, nextWeek);

		if (!suffix.isBlank()) {
			if ("VISIT_DATE".equals(workLog.getReportDisplayType())) {
				line.append(suffix);
			} else {
				line.append(" ").append(suffix);
			}
		}

		return line.toString();
	}

	private String buildSuffix(WorkLog workLog, boolean nextWeek) {
		String reportDisplayType = workLog.getReportDisplayType() == null ? "PROGRESS" : workLog.getReportDisplayType();

		return switch (reportDisplayType) {
			case "VISIT_DATE" -> "(" + workLog.getStartAt().toLocalDate().format(DATE_FORMATTER) + ")";
			case "DONE_STATUS" -> "(" + defaultValue(workLog.getStatus(), "완료") + ")";
			case "DUE_DATE" -> "(~" + workLog.getEndAt().toLocalDate().format(DATE_FORMATTER) + ")";
			case "NONE" -> "";
			default -> buildProgressSuffix(workLog, nextWeek);
		};
	}

	private String buildProgressSuffix(WorkLog workLog, boolean nextWeek) {
		if ("완료".equals(workLog.getStatus())) {
			return "(완료)";
		}

		if (workLog.getProgressRate() != null) {
			return "(" + workLog.getProgressRate() + "%)";
		}

		if (nextWeek) {
			return "(~" + workLog.getEndAt().toLocalDate().format(DATE_FORMATTER) + ")";
		}

		return "";
	}

	private String toWorkType(String category) {
		return switch (category == null ? "" : category) {
			case "개발", "문서" -> "프로젝트 일반";
			case "지원" -> "프로젝트 지원협조";
			case "유지보수" -> "유지보수";
			case "공유" -> "공유";
			default -> "일반업무";
		};
	}

	private String normalizeProjectName(String projectName) {
		if (projectName == null || projectName.isBlank()) {
			return "공통";
		}

		return projectName.trim();
	}

	private String defaultValue(String value, String defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}

		return value.trim();
	}

}
