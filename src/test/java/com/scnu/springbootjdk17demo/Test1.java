package com.scnu.springbootjdk17demo;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Test1 {
    // 在任意测试类或 main 方法里跑一下
    @Test
    void generatePassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("test123"));
    }
}
