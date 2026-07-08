package com.smartfarming.service;

import com.smartfarming.entity.SysUser;
import com.smartfarming.mapper.SysUserMapper;
import com.smartfarming.security.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void testLoginSuccess() {
        // 准备测试数据
        SysUser mockUser = new SysUser();
        mockUser.setUserId(1L);
        mockUser.setUsername("admin");
        mockUser.setPassword("$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36zLlWrkSivbj7M7Pv/7O3y");
        mockUser.setRole("admin");

        when(userMapper.selectOne(any())).thenReturn(mockUser);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(jwtUtils.generateToken(any(), any(), any())).thenReturn("test-token");

        // 执行测试
        Map<String, Object> result = userService.login("admin", "admin123");

        // 验证结果
        assertNotNull(result);
        assertEquals("test-token", result.get("token"));
        assertEquals("admin", result.get("username"));
    }

    @Test
    void testLoginFailure() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            userService.login("wronguser", "wrongpass");
        });
    }

    @Test
    void testRegisterSuccess() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("encoded-password");
        when(userMapper.insert(any())).thenReturn(1);

        assertDoesNotThrow(() -> {
            userService.register("newuser", "password123", "user");
        });
    }

    @Test
    void testRegisterDuplicateUsername() {
        when(userMapper.selectCount(any())).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> {
            userService.register("admin", "password123", "user");
        });
    }
}
