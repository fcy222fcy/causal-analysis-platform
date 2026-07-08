package com.smartfarming.controller;

import com.smartfarming.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void testLogin() {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("token", "test-token");
        mockResult.put("username", "admin");
        mockResult.put("role", "admin");

        when(userService.login(any(), any())).thenReturn(mockResult);

        Map<String, String> params = new HashMap<>();
        params.put("username", "admin");
        params.put("password", "admin123");

        Map<String, Object> result = userController.login(params);

        assertNotNull(result);
        assertEquals("test-token", result.get("token"));
        assertEquals("admin", result.get("username"));
    }

    @Test
    void testRegister() {
        Map<String, String> params = new HashMap<>();
        params.put("username", "newuser");
        params.put("password", "password123");
        params.put("role", "user");

        Map<String, Object> result = userController.register(params);

        assertNotNull(result);
        assertEquals("注册成功", result.get("message"));
    }
}
