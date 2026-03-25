package com.scnu.springbootjdk17demo.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scnu.springbootjdk17demo.config.DataSourceContextHolder;
import com.scnu.springbootjdk17demo.dto.LoginResponse;
import com.scnu.springbootjdk17demo.entity.SysUser;
import com.scnu.springbootjdk17demo.mapper.SysUserMapper;
import com.scnu.springbootjdk17demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    public LoginResponse login(String username, String password) {

        // ① 查询走从库
        DataSourceContextHolder.set("replica");
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
        );
        DataSourceContextHolder.clear();

        if (user == null) {
            return LoginResponse.builder().code(401).message("用户不存在").build();
        }

        if (user.getStatus() == 0) {
            return LoginResponse.builder().code(403).message("账号已禁用").build();
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return LoginResponse.builder().code(401).message("密码错误").build();
        }

        // ② 更新 last_login 走主库
        DataSourceContextHolder.set("primary");
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLogin(OffsetDateTime.now());
        update.setUpdatedAt(OffsetDateTime.now());   // ← 加这一行
        userMapper.updateById(update);
        DataSourceContextHolder.clear();

        log.info("用户 [{}] 登录成功", username);

        return LoginResponse.builder()
                .code(200)
                .message("登录成功")
                .token(jwtUtil.generate(username))
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }
}