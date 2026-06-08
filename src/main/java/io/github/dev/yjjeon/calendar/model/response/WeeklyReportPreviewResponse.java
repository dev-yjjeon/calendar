package io.github.dev.yjjeon.calendar.model.response;

import java.util.List;

public record WeeklyReportPreviewResponse(
	List<WorkLogResponse> candidates,
	List<WeeklyReportRow> rows
) {
}
