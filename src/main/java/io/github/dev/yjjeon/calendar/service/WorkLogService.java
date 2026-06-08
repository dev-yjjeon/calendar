package io.github.dev.yjjeon.calendar.service;

import io.github.dev.yjjeon.calendar.mapper.WorkLogMapper;
import io.github.dev.yjjeon.calendar.model.entity.WorkLog;
import io.github.dev.yjjeon.calendar.model.request.WorkLogRequest;
import io.github.dev.yjjeon.calendar.model.request.WorkLogTimeRequest;
import io.github.dev.yjjeon.calendar.model.response.WorkLogResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkLogService {

	private final WorkLogMapper workLogMapper;

	public WorkLogService(WorkLogMapper workLogMapper) {
		this.workLogMapper = workLogMapper;
	}

	@Transactional(readOnly = true)
	public List<WorkLogResponse> findBetween(LocalDateTime start, LocalDateTime end) {
		return workLogMapper.findBetween(start, end)
			.stream()
			.map(WorkLogResponse::from)
			.toList();
	}

	@Transactional
	public WorkLogResponse create(WorkLogRequest request) {
		WorkLog workLog = new WorkLog();
		applyRequest(workLog, request);
		workLogMapper.insert(workLog);
		return WorkLogResponse.from(workLog);
	}

	@Transactional
	public WorkLogResponse update(Long id, WorkLogRequest request) {
		WorkLog workLog = findById(id);
		applyRequest(workLog, request);
		workLogMapper.update(workLog);
		return WorkLogResponse.from(workLog);
	}

	@Transactional
	public WorkLogResponse updateTime(Long id, WorkLogTimeRequest request) {
		WorkLog workLog = findById(id);
		workLog.setStartAt(request.getStart());
		workLog.setEndAt(request.getEnd());
		workLog.setAllDay(request.isAllDay());
		workLogMapper.updateTime(workLog);
		return WorkLogResponse.from(workLog);
	}

	@Transactional
	public void delete(Long id) {
		findById(id);
		workLogMapper.delete(id);
	}

	@Transactional(readOnly = true)
	public List<WorkLogResponse> findIncomplete() {
		return workLogMapper.findIncomplete()
			.stream()
			.map(WorkLogResponse::from)
			.toList();
	}

	private WorkLog findById(Long id) {
		WorkLog workLog = workLogMapper.findById(id);

		if (workLog == null) {
			throw new IllegalArgumentException("업무 기록을 찾을 수 없습니다.");
		}

		return workLog;
	}

	private void applyRequest(WorkLog workLog, WorkLogRequest request) {
		workLog.setTitle(request.getTitle().trim());
		workLog.setContent(blankToNull(request.getContent()));
		workLog.setStartAt(request.getStart());
		workLog.setEndAt(request.getEnd());
		workLog.setAllDay(request.isAllDay());
		workLog.setProjectName(blankToNull(request.getProjectName()));
		workLog.setCategory(defaultValue(request.getCategory(), "기타"));
		workLog.setStatus(defaultValue(request.getStatus(), "계획"));
		workLog.setProgressRate(resolveProgressRate(request));
		workLog.setReportDisplayType(defaultValue(request.getReportDisplayType(), "PROGRESS"));
		workLog.setReportIncluded(request.isReportIncluded());
	}

	private Integer resolveProgressRate(WorkLogRequest request) {
		if (!"진행중".equals(request.getStatus())) {
			return null;
		}

		return request.getProgressRate();
	}

	private String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}

	private String defaultValue(String value, String defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}

		return value.trim();
	}

}
