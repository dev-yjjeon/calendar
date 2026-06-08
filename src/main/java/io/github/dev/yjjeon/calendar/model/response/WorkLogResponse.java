package io.github.dev.yjjeon.calendar.model.response;

import io.github.dev.yjjeon.calendar.model.entity.WorkLog;
import java.time.LocalDateTime;

public record WorkLogResponse(
	Long id,
	String title,
	String content,
	LocalDateTime start,
	LocalDateTime end,
	boolean allDay,
	String projectName,
	String category,
	String status,
	Integer progressRate,
	String reportDisplayType,
	boolean reportIncluded
) {

	public static WorkLogResponse from(WorkLog workLog) {
		return new WorkLogResponse(
			workLog.getId(),
			workLog.getTitle(),
			workLog.getContent(),
			workLog.getStartAt(),
			workLog.getEndAt(),
			workLog.isAllDay(),
			workLog.getProjectName(),
			workLog.getCategory(),
			workLog.getStatus(),
			workLog.getProgressRate(),
			workLog.getReportDisplayType(),
			workLog.isReportIncluded()
		);
	}

}
