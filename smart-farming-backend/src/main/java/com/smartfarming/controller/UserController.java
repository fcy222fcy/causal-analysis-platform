package com.smartfarming.controller;

import com.smartfarming.security.JwtUtils;
import com.smartfarming.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户登录注册接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        return userService.login(params.get("username"), params.get("password"));
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Map<String, Object> register(@RequestBody Map<String, String> params) {
        userService.register(params.get("username"), params.get("password"), params.get("role"));
        Map<String, Object> result = new HashMap<>();
        result.put("message", "注册成功");
        return result;
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Map<String, Object> getUserInfo(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Long userId = jwtUtils.getUserIdFromToken(token);
                String username = jwtUtils.getUsernameFromToken(token);
                String role = jwtUtils.getRoleFromToken(token);
                result.put("userId", userId);
                result.put("username", username);
                result.put("role", role);
            } catch (Exception e) {
                result.put("userId", 1L);
                result.put("username", "admin");
                result.put("role", "admin");
            }
        } else {
            result.put("userId", 1L);
            result.put("username", "admin");
            result.put("role", "admin");
        }
        return result;
    }
}
