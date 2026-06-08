package io.github.dev.yjjeon.calendar.mapper;

import io.github.dev.yjjeon.calendar.model.entity.WorkLog;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WorkLogMapper {

	@Select("""
		SELECT
			id,
			user_id,
			title,
			content,
			start_at,
			end_at,
			all_day,
			project_name,
			category,
			status,
			progress_rate,
			report_display_type,
			report_included
		FROM work_log
		WHERE user_id = #{userId}
		  AND start_at < #{endAt}
		  AND end_at > #{startAt}
		ORDER BY start_at ASC
		""")
	List<WorkLog> findBetween(@Param("userId") Long userId, @Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

	@Select("""
		SELECT
			id,
			user_id,
			title,
			content,
			start_at,
			end_at,
			all_day,
			project_name,
			category,
			status,
			progress_rate,
			report_display_type,
			report_included
		FROM work_log
		WHERE id = #{id} AND user_id = #{userId}
		""")
	WorkLog findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	@Insert("""
		INSERT INTO work_log (
			user_id,
			title,
			content,
			start_at,
			end_at,
			all_day,
			project_name,
			category,
			status,
			progress_rate,
			report_display_type,
			report_included
		) VALUES (
			#{userId},
			#{title},
			#{content},
			#{startAt},
			#{endAt},
			#{allDay},
			#{projectName},
			#{category},
			#{status},
			#{progressRate},
			#{reportDisplayType},
			#{reportIncluded}
		)
		""")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	void insert(WorkLog workLog);

	@Update("""
		UPDATE work_log
		SET title = #{title},
		    content = #{content},
		    start_at = #{startAt},
		    end_at = #{endAt},
		    all_day = #{allDay},
		    project_name = #{projectName},
		    category = #{category},
		    status = #{status},
		    progress_rate = #{progressRate},
		    report_display_type = #{reportDisplayType},
		    report_included = #{reportIncluded}
		WHERE id = #{id} AND user_id = #{userId}
		""")
	void update(WorkLog workLog);

	@Update("""
		UPDATE work_log
		SET start_at = #{startAt},
		    end_at = #{endAt},
		    all_day = #{allDay}
		WHERE id = #{id} AND user_id = #{userId}
		""")
	void updateTime(WorkLog workLog);

	@Delete("DELETE FROM work_log WHERE id = #{id} AND user_id = #{userId}")
	void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	@Select("""
		SELECT
			id,
			user_id,
			title,
			content,
			start_at,
			end_at,
			all_day,
			project_name,
			category,
			status,
			progress_rate,
			report_display_type,
			report_included
		FROM work_log
		WHERE user_id = #{userId} AND status != '완료'
		ORDER BY id DESC
		""")
	List<WorkLog> findIncompleteByUserId(Long userId);

}
