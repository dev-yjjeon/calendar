package io.github.dev.yjjeon.calendar.model.response;

public record WeeklyReportRow(
	String workType,
	String thisWeek,
	String nextWeek
) {
}
