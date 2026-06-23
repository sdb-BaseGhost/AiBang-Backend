package org.sdb.aiban.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sdb.aiban.common.result.Result;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/public")
    public Result<Map<String, String>> publicEndpoint() {
        return Result.success(Map.of("message", "这是一个公开接口，无需登录"));
    }

    @GetMapping("/protected")
    public Result<Map<String, String>> protectedEndpoint() {
        return Result.success(Map.of("message", "这是一个需要登录的接口"));
    }
}