package io.github.dev.yjjeon.calendar.model.request;

import java.time.LocalDate;
import java.util.List;

public record WeeklyReportRequest(
	LocalDate reportDate,
	String writer,
	LocalDate weekStart,
	LocalDate weekEnd,
	LocalDate nextWeekStart,
	LocalDate nextWeekEnd,
	List<InstructionData> instructions,
	List<PerformanceData> performances,
	List<ProgressData> progresses,
	List<DelayData> delays
) {
	public record InstructionData(
		Integer no,
		String content,
		String directedDate,
		String dueDate,
		String completeDate,
		String assignee,
		String status,
		String note
	) {}

	public record PerformanceData(
		String workType,
		String thisWeek,
		String nextWeek
	) {}

	public record ProgressData(
		Integer no,
		String projectName,
		Integer startRate,
		Integer analysisRate,
		Integer designRate,
		Integer developRate,
		Integer testRate,
		Integer stabilizationRate,
		String status
	) {}

	public record DelayData(
		Integer no,
		String projectName,
		String category,
		String issueContent,
		String recoveryPlan
	) {}
}
