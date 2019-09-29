package com.h2t.study.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试类，用于测试项目是否正常构建
 *
 * @author hetiantian
 * @version 1.0
 * @Date 2019/08/01 14:23
 */
@RestController
public class TestController {
    @GetMapping("/hello")
    public Object hello() {
        return "hello world";
    }
}
