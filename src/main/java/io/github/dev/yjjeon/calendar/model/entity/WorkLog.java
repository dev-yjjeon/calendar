package io.github.dev.yjjeon.calendar.model.entity;

import java.time.LocalDateTime;

public class WorkLog {

	private Long id;

	private String title;

	private String content;

	private LocalDateTime startAt;

	private LocalDateTime endAt;

	private boolean allDay;

	private String projectName;

	private String category;

	private String status;

	private Integer progressRate;

	private String reportDisplayType;

	private boolean reportIncluded = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public LocalDateTime getStartAt() {
		return startAt;
	}

	public void setStartAt(LocalDateTime startAt) {
		this.startAt = startAt;
	}

	public LocalDateTime getEndAt() {
		return endAt;
	}

	public void setEndAt(LocalDateTime endAt) {
		this.endAt = endAt;
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
