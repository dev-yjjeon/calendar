package io.github.dev.yjjeon.calendar.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class WorkLogRequest {

	@NotBlank
	private String title;

	private String content;

	@NotNull
	private LocalDateTime start;

	@NotNull
	private LocalDateTime end;

	private boolean allDay;

	private String projectName;

	private String category;

	private String status;

	@Min(0)
	@Max(100)
	private Integer progressRate;

	private String reportDisplayType;

	private boolean reportIncluded = true;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public void setStart(LocalDateTime start) {
		this.start = start;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public void setEnd(LocalDateTime end) {
		this.end = end;
	}

	public boolean isAllDay() {
		return allDay;
	}

	public void setAllDay(boolean allDay) {
		this.allDay = allDay;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getProgressRate() {
		return progressRate;
	}

	public void setProgressRate(Integer progressRate) {
		this.progressRate = progressRate;
	}

	public String getReportDisplayType() {
		return reportDisplayType;
	}

	public void setReportDisplayType(String reportDisplayType) {
		this.reportDisplayType = reportDisplayType;
	}

	public boolean isReportIncluded() {
		return reportIncluded;
	}

	public void setReportIncluded(boolean reportIncluded) {
		this.reportIncluded = reportIncluded;
	}

}
