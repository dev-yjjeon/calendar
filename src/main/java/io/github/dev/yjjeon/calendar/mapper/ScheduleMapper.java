package io.github.dev.yjjeon.calendar.mapper;

import io.github.dev.yjjeon.calendar.model.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ScheduleMapper {
    List<Schedule> findAllByUserId(Long userId);
    Schedule findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    int insert(Schedule schedule);
    int update(Schedule schedule);
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
