package io.github.dev.yjjeon.calendar.service;

import io.github.dev.yjjeon.calendar.mapper.UserMapper;
import io.github.dev.yjjeon.calendar.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public void signup(User user) {
        if (userMapper.findByUsername(user.getUsername()) != null) {
            throw new IllegalArgumentException("이미 존재하는 사용자 ID입니다.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }
}
