package io.github.dev.yjjeon.calendar.mapper;

import io.github.dev.yjjeon.calendar.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    void insert(User user);
}
