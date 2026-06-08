package io.github.dev.yjjeon.calendar.service;

import io.github.dev.yjjeon.calendar.mapper.WorkLogMapper;
import io.github.dev.yjjeon.calendar.model.entity.WorkLog;
import io.github.dev.yjjeon.calendar.model.request.WeeklyReportRequest;
import io.github.dev.yjjeon.calendar.model.response.WeeklyReportPreviewResponse;
import io.github.dev.yjjeon.calendar.model.response.WeeklyReportRow;
import io.github.dev.yjjeon.calendar.model.response.WorkLogResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
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
		List<WorkLog> rawThisWeekLogs = findReportLogs(weekStart, weekEnd);
		List<WorkLog> rawNextWeekLogs = findReportLogs(nextWeekStart, nextWeekEnd);

		List<WorkLog> thisWeekLogs = filterLatestLogs(rawThisWeekLogs);
		List<WorkLog> nextWeekLogs = filterLatestLogs(rawNextWeekLogs);

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

	private List<WorkLog> filterLatestLogs(List<WorkLog> logs) {
		Map<String, WorkLog> latestLogMap = new LinkedHashMap<>();
		for (WorkLog log : logs) {
			String key = normalizeProjectName(log.getProjectName()) + "|||" + log.getTitle().trim();
			WorkLog existing = latestLogMap.get(key);
			if (existing == null || log.getStartAt().isAfter(existing.getStartAt())) {
				latestLogMap.put(key, log);
			}
		}
		return new ArrayList<>(latestLogMap.values());
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
		if ("완료".equals(workLog.getStatus()) || (workLog.getProgressRate() != null && workLog.getProgressRate() == 100)) {
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

	public void generateExcel(WeeklyReportRequest request, OutputStream outputStream) throws IOException {
		try (XSSFWorkbook workbook = new XSSFWorkbook()) {
			XSSFSheet sheet = workbook.createSheet("주간회의보고서");

			// === 폰트 정의 ===
			XSSFFont defaultFont = workbook.createFont();
			defaultFont.setFontName("맑은 고딕");
			defaultFont.setFontHeightInPoints((short) 10);

			XSSFFont titleFont = workbook.createFont();
			titleFont.setFontName("맑은 고딕");
			titleFont.setFontHeightInPoints((short) 16);
			titleFont.setBold(true);

			XSSFFont sectionLabelFont = workbook.createFont();
			sectionLabelFont.setFontName("맑은 고딕");
			sectionLabelFont.setFontHeightInPoints((short) 10);
			sectionLabelFont.setBold(true);

			XSSFFont headerFont = workbook.createFont();
			headerFont.setFontName("맑은 고딕");
			headerFont.setFontHeightInPoints((short) 10);
			headerFont.setBold(true);

			XSSFFont metaLabelFont = workbook.createFont();
			metaLabelFont.setFontName("맑은 고딕");
			metaLabelFont.setFontHeightInPoints((short) 10);
			metaLabelFont.setBold(true);

			// === 색상 정의 ===
			XSSFColor grayBg = new XSSFColor(new byte[]{(byte) 217, (byte) 217, (byte) 217}, null);     // #D9D9D9
			XSSFColor lightGrayBg = new XSSFColor(new byte[]{(byte) 242, (byte) 242, (byte) 242}, null); // #F2F2F2
			XSSFColor yellowBg = new XSSFColor(new byte[]{(byte) 255, (byte) 255, (byte) 0}, null);      // #FFFF00
			XSSFColor borderColor = new XSSFColor(new byte[]{(byte) 0, (byte) 0, (byte) 0}, null);       // Black

			// === 스타일 정의 ===

			// 타이틀 스타일
			CellStyle titleStyle = workbook.createCellStyle();
			titleStyle.setFont(titleFont);
			titleStyle.setAlignment(HorizontalAlignment.CENTER);
			titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			titleStyle.setFillForegroundColor(lightGrayBg);
			titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			setBlackBorders(titleStyle);

			// 메타 라벨 스타일 (작성일자, 작성자 등)
			CellStyle metaLabelStyle = workbook.createCellStyle();
			metaLabelStyle.setFont(metaLabelFont);
			metaLabelStyle.setAlignment(HorizontalAlignment.CENTER);
			metaLabelStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			metaLabelStyle.setFillForegroundColor(lightGrayBg);
			metaLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			setBlackBorders(metaLabelStyle);

			// 메타 값 스타일
			CellStyle metaValueStyle = workbook.createCellStyle();
			metaValueStyle.setFont(defaultFont);
			metaValueStyle.setAlignment(HorizontalAlignment.CENTER);
			metaValueStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			setBlackBorders(metaValueStyle);

			// 헤더 스타일 (회색 배경 + 검정 굵은 글씨)
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFont(headerFont);
			headerStyle.setAlignment(HorizontalAlignment.CENTER);
			headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			headerStyle.setFillForegroundColor(grayBg);
			headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			setBlackBorders(headerStyle);

			// 일반 데이터 스타일 (테두리 + 왼쪽정렬)
			CellStyle dataStyle = workbook.createCellStyle();
			dataStyle.setFont(defaultFont);
			dataStyle.setVerticalAlignment(VerticalAlignment.TOP);
			dataStyle.setWrapText(true);
			setBlackBorders(dataStyle);

			// 일반 데이터 스타일 (테두리 + 가운데정렬)
			CellStyle dataCenterStyle = workbook.createCellStyle();
			dataCenterStyle.setFont(defaultFont);
			dataCenterStyle.setAlignment(HorizontalAlignment.CENTER);
			dataCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			setBlackBorders(dataCenterStyle);

			// 섹션2 구분열 스타일 (연회색 배경 + 가운데 + 굵은 글씨)
			CellStyle workTypeStyle = workbook.createCellStyle();
			workTypeStyle.setFont(headerFont);
			workTypeStyle.setAlignment(HorizontalAlignment.CENTER);
			workTypeStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			workTypeStyle.setWrapText(true);
			workTypeStyle.setFillForegroundColor(lightGrayBg);
			workTypeStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			setBlackBorders(workTypeStyle);

			// 노란 배경 스타일 (진척률 값 있을 때)
			CellStyle yellowStyle = workbook.createCellStyle();
			yellowStyle.setFont(defaultFont);
			yellowStyle.setAlignment(HorizontalAlignment.CENTER);
			yellowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			yellowStyle.setFillForegroundColor(yellowBg);
			yellowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			setBlackBorders(yellowStyle);

			// 섹션 라벨 스타일 (테두리 없음)
			CellStyle sectionLabelStyle = workbook.createCellStyle();
			sectionLabelStyle.setFont(sectionLabelFont);
			sectionLabelStyle.setVerticalAlignment(VerticalAlignment.CENTER);

			// 빈 셀 스타일 (테두리만)
			CellStyle borderOnlyStyle = workbook.createCellStyle();
			borderOnlyStyle.setFont(defaultFont);
			setBlackBorders(borderOnlyStyle);

			// 날짜 포맷용
			String weekRange = formatDateRange(request.weekStart(), request.weekEnd());
			String nextWeekRange = formatDateRange(request.nextWeekStart(), request.nextWeekEnd());

			int rowIdx = 0;

			// ===== 타이틀 행 (0행) =====
			// A~F: 타이틀 병합, G: 작성일자, H: 날짜값, I: 작성자, J: 이름값
			Row titleRow = sheet.createRow(rowIdx);
			titleRow.setHeightInPoints(36);
			createStyledCell(titleRow, 0, "개발팀 주간회의 보고서", titleStyle);
			for (int i = 1; i <= 5; i++) createStyledCell(titleRow, i, "", titleStyle);
			createStyledCell(titleRow, 6, "작성일자", metaLabelStyle);
			createStyledCell(titleRow, 7, request.reportDate() != null ? request.reportDate().toString() : "", metaValueStyle);
			createStyledCell(titleRow, 8, "작성자", metaLabelStyle);
			createStyledCell(titleRow, 9, request.writer() != null ? request.writer() : "", metaValueStyle);
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx, rowIdx, 0, 5));

			// 열 너비 설정 (A~J, 10열)
			sheet.setColumnWidth(0, 5 * 256);    // A: NO
			sheet.setColumnWidth(1, 14 * 256);   // B: 업무수명사항 / 구분
			sheet.setColumnWidth(2, 12 * 256);   // C: 지시일 / 착수
			sheet.setColumnWidth(3, 12 * 256);   // D: 완료예정일 / 분석
			sheet.setColumnWidth(4, 25 * 256);   // E: 완료일 / 금주실적(넓게)
			sheet.setColumnWidth(5, 10 * 256);   // F: 담당자 / 설계
			sheet.setColumnWidth(6, 10 * 256);   // G: 진행상태 / 개발
			sheet.setColumnWidth(7, 10 * 256);   // H: 비고 / 테스트
			sheet.setColumnWidth(8, 10 * 256);   // I: 안정화
			sheet.setColumnWidth(9, 10 * 256);   // J: 진행상태

			rowIdx++;

			// ===== 1. 업무 수명사항 =====
			Row s1Label = sheet.createRow(rowIdx++);
			s1Label.setHeightInPoints(18);
			createStyledCell(s1Label, 0, "1. 업무 수명사항", sectionLabelStyle);

			Row s1Header = sheet.createRow(rowIdx++);
			s1Header.setHeightInPoints(24);
			String[] s1Cols = {"NO", "업무 수명사항 [대표이사, 임원]", "", "지시일", "완료\n예정일", "완료일", "담당자", "진행상태", "비고"};
			createStyledCell(s1Header, 0, s1Cols[0], headerStyle);
			createStyledCell(s1Header, 1, s1Cols[1], headerStyle);
			createStyledCell(s1Header, 2, "", headerStyle);
			createStyledCell(s1Header, 3, "지시일", headerStyle);
			createStyledCell(s1Header, 4, "완료\n예정일", headerStyle);
			createStyledCell(s1Header, 5, "완료일", headerStyle);
			createStyledCell(s1Header, 6, "담당자", headerStyle);
			createStyledCell(s1Header, 7, "진행상태", headerStyle);
			createStyledCell(s1Header, 8, "비고", headerStyle);
			// 빈 셀도 채우기
			createStyledCell(s1Header, 9, "", headerStyle);
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 1, 2));
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 8, 9));

			// 데이터 행 (최소 5행 보장)
			int s1DataCount = (request.instructions() != null) ? request.instructions().size() : 0;
			int s1Rows = Math.max(s1DataCount, 5);
			for (int i = 0; i < s1Rows; i++) {
				Row r = sheet.createRow(rowIdx++);
				r.setHeightInPoints(24);
				if (i < s1DataCount) {
					var inst = request.instructions().get(i);
					createStyledCell(r, 0, String.valueOf(inst.no()), dataCenterStyle);
					createStyledCell(r, 1, inst.content(), dataStyle);
					createStyledCell(r, 2, "", dataStyle);
					createStyledCell(r, 3, inst.directedDate(), dataCenterStyle);
					createStyledCell(r, 4, inst.dueDate(), dataCenterStyle);
					createStyledCell(r, 5, inst.completeDate(), dataCenterStyle);
					createStyledCell(r, 6, inst.assignee(), dataCenterStyle);
					createStyledCell(r, 7, inst.status(), dataCenterStyle);
					createStyledCell(r, 8, inst.note(), dataStyle);
					createStyledCell(r, 9, "", dataStyle);
				} else {
					createStyledCell(r, 0, String.valueOf(i + 1), dataCenterStyle);
					for (int j = 1; j <= 9; j++) createStyledCell(r, j, "", dataStyle);
				}
				sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 1, 2));
				sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 8, 9));
			}

			rowIdx++; // 빈줄

			// ===== 2. 개발부 실적 및 계획 =====
			Row s2Label = sheet.createRow(rowIdx++);
			s2Label.setHeightInPoints(18);
			createStyledCell(s2Label, 0, "2. 개발부 실적 및 계획", sectionLabelStyle);

			Row s2Header = sheet.createRow(rowIdx++);
			s2Header.setHeightInPoints(24);
			createStyledCell(s2Header, 0, "NO", headerStyle);
			createStyledCell(s2Header, 1, "구분", headerStyle);
			createStyledCell(s2Header, 2, "금주 실적 (" + weekRange + ")", headerStyle);
			createStyledCell(s2Header, 3, "", headerStyle);
			createStyledCell(s2Header, 4, "", headerStyle);
			createStyledCell(s2Header, 5, "", headerStyle);
			createStyledCell(s2Header, 6, "차주 계획 (" + nextWeekRange + ")", headerStyle);
			createStyledCell(s2Header, 7, "", headerStyle);
			createStyledCell(s2Header, 8, "", headerStyle);
			createStyledCell(s2Header, 9, "", headerStyle);
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 2, 5));
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 6, 9));

			// 5개 업무 유형 (프로젝트 일반 ~ 공유)
			String[] workTypeLabels = {"프로젝트\n일반", "프로젝트\n지원협조", "유지보수", "일반업무", "공유"};
			String[] workTypeKeys = {"프로젝트 일반", "프로젝트 지원협조", "유지보수", "일반업무", "공유"};

			for (int i = 0; i < workTypeLabels.length; i++) {
				Row r = sheet.createRow(rowIdx++);
				r.setHeightInPoints(100); // 충분한 높이

				createStyledCell(r, 0, String.valueOf(i + 1), dataCenterStyle);
				createStyledCell(r, 1, workTypeLabels[i], workTypeStyle);

				// 금주 실적 / 차주 계획 데이터 찾기
				String thisWeekText = "";
				String nextWeekText = "";
				if (request.performances() != null) {
					for (var perf : request.performances()) {
						if (workTypeKeys[i].equals(perf.workType())) {
							thisWeekText = perf.thisWeek() != null ? perf.thisWeek() : "";
							nextWeekText = perf.nextWeek() != null ? perf.nextWeek() : "";
							break;
						}
					}
				}

				createStyledCell(r, 2, thisWeekText, dataStyle);
				for (int j = 3; j <= 5; j++) createStyledCell(r, j, "", dataStyle);
				createStyledCell(r, 6, nextWeekText, dataStyle);
				for (int j = 7; j <= 9; j++) createStyledCell(r, j, "", dataStyle);
				sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 2, 5));
				sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 6, 9));
			}

			rowIdx++; // 빈줄

			// ===== 3. 프로젝트 진행현황 =====
			Row s3Label = sheet.createRow(rowIdx++);
			s3Label.setHeightInPoints(18);
			createStyledCell(s3Label, 0, "3. 프로젝트 진행현황", sectionLabelStyle);

			Row s3Header = sheet.createRow(rowIdx++);
			s3Header.setHeightInPoints(24);
			String[] s3Cols = {"NO", "프로젝트", "착수", "분석", "설계", "개발", "테스트", "안정화", "진행상태"};
			for (int i = 0; i < s3Cols.length; i++) {
				createStyledCell(s3Header, i, s3Cols[i], headerStyle);
			}
			createStyledCell(s3Header, 9, "", headerStyle);
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 8, 9));

			int s3DataCount = (request.progresses() != null) ? request.progresses().size() : 0;
			int s3Rows = Math.max(s3DataCount, 2);
			for (int i = 0; i < s3Rows; i++) {
				Row r = sheet.createRow(rowIdx++);
				r.setHeightInPoints(24);
				if (i < s3DataCount) {
					var prog = request.progresses().get(i);
					createStyledCell(r, 0, String.valueOf(prog.no()), dataCenterStyle);
					createStyledCell(r, 1, prog.projectName(), dataStyle);
					createRateCell(r, 2, prog.startRate(), dataCenterStyle, yellowStyle);
					createRateCell(r, 3, prog.analysisRate(), dataCenterStyle, yellowStyle);
					createRateCell(r, 4, prog.designRate(), dataCenterStyle, yellowStyle);
					createRateCell(r, 5, prog.developRate(), dataCenterStyle, yellowStyle);
					createRateCell(r, 6, prog.testRate(), dataCenterStyle, yellowStyle);
					createRateCell(r, 7, prog.stabilizationRate(), dataCenterStyle, yellowStyle);
					createStyledCell(r, 8, prog.status(), dataCenterStyle);
					createStyledCell(r, 9, "", dataCenterStyle);
				} else {
					createStyledCell(r, 0, String.valueOf(i + 1), dataCenterStyle);
					for (int j = 1; j <= 9; j++) createStyledCell(r, j, "", dataStyle);
				}
				sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 8, 9));
			}

			rowIdx++; // 빈줄

			// ===== 4. 지연프로젝트 =====
			Row s4Label = sheet.createRow(rowIdx++);
			s4Label.setHeightInPoints(18);
			createStyledCell(s4Label, 0, "4. 지연프로젝트", sectionLabelStyle);

			Row s4Header = sheet.createRow(rowIdx++);
			s4Header.setHeightInPoints(24);
			createStyledCell(s4Header, 0, "NO", headerStyle);
			createStyledCell(s4Header, 1, "프로젝트", headerStyle);
			createStyledCell(s4Header, 2, "구분", headerStyle);
			createStyledCell(s4Header, 3, "이슈내용", headerStyle);
			createStyledCell(s4Header, 4, "", headerStyle);
			createStyledCell(s4Header, 5, "Gap 극복방안", headerStyle);
			for (int i = 6; i <= 9; i++) createStyledCell(s4Header, i, "", headerStyle);
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 3, 4));
			sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 5, 9));

			int s4DataCount = (request.delays() != null) ? request.delays().size() : 0;
			int s4Rows = Math.max(s4DataCount, 1);
			for (int i = 0; i < s4Rows; i++) {
				Row r = sheet.createRow(rowIdx++);
				r.setHeightInPoints(30);
				if (i < s4DataCount) {
					var delay = request.delays().get(i);
					createStyledCell(r, 0, String.valueOf(delay.no()), dataCenterStyle);
					createStyledCell(r, 1, delay.projectName(), dataStyle);
					createStyledCell(r, 2, delay.category(), dataCenterStyle);
					createStyledCell(r, 3, delay.issueContent(), dataStyle);
					createStyledCell(r, 4, "", dataStyle);
					createStyledCell(r, 5, delay.recoveryPlan(), dataStyle);
					for (int j = 6; j <= 9; j++) createStyledCell(r, j, "", dataStyle);
				} else {
					createStyledCell(r, 0, String.valueOf(i + 1), dataCenterStyle);
					for (int j = 1; j <= 9; j++) createStyledCell(r, j, "", dataStyle);
				}
				sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 3, 4));
				sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 5, 9));
			}

			workbook.write(outputStream);
		}
	}

	private void createRateCell(Row row, int column, Integer rate, CellStyle normalStyle, CellStyle highlightStyle) {
		Cell cell = row.createCell(column);
		if (rate != null) {
			cell.setCellValue(rate + "%");
			cell.setCellStyle(highlightStyle);
		} else {
			cell.setCellValue("");
			cell.setCellStyle(normalStyle);
		}
	}

	private String formatDateRange(LocalDate start, LocalDate end) {
		if (start == null || end == null) return "";
		return start.getMonthValue() + "." + start.getDayOfMonth() + " ~ " + end.getMonthValue() + "." + end.getDayOfMonth();
	}

	private void createStyledCell(Row row, int column, String value, CellStyle style) {
		Cell cell = row.createCell(column);
		cell.setCellValue(value == null ? "" : value);
		cell.setCellStyle(style);
	}

	private void setBlackBorders(CellStyle style) {
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
	}

}
