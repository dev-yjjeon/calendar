package io.github.dev.yjjeon.calendar.model.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class WorkLogTimeRequest {

	@NotNull
	private LocalDateTime start;

	@NotNull
	private LocalDateTime end;

	private boolean allDay;

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

}
