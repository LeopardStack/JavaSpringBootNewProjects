package com.scnu.springbootjdk17demo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.scnu.springbootjdk17demo.config.DataSourceContextHolder;
import com.scnu.springbootjdk17demo.dto.LoginResponse;
import com.scnu.springbootjdk17demo.dto.UserInfoResponse;
import com.scnu.springbootjdk17demo.dto.UserInfoResponse.MenuVO;
import com.scnu.springbootjdk17demo.entity.SysPermission;
import com.scnu.springbootjdk17demo.entity.SysRole;
import com.scnu.springbootjdk17demo.entity.SysUser;
import com.scnu.springbootjdk17demo.mapper.SysPermissionMapper;
import com.scnu.springbootjdk17demo.mapper.SysRoleMapper;
import com.scnu.springbootjdk17demo.mapper.SysUserMapper;
import com.scnu.springbootjdk17demo.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper       userMapper;
    private final SysRoleMapper       roleMapper;
    private final SysPermissionMapper permMapper;
    private final PasswordEncoder     passwordEncoder;
    private final JwtUtil             jwtUtil;
    private final TokenService tokenService;

    /** 登录 */
    public LoginResponse login(String username, String password) {
        DataSourceContextHolder.set("replica");
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        DataSourceContextHolder.clear();

        if (user == null)
            return LoginResponse.builder().code(401).message("用户不存在").build();
        if (user.getStatus() == 0)
            return LoginResponse.builder().code(403).message("账号已禁用").build();
        if (!passwordEncoder.matches(password, user.getPassword()))
            return LoginResponse.builder().code(401).message("密码错误").build();

        DataSourceContextHolder.set("primary");
        SysUser upd = new SysUser();
        upd.setId(user.getId());
        upd.setLastLogin(OffsetDateTime.now());
        upd.setUpdatedAt(OffsetDateTime.now());
        userMapper.updateById(upd);
        DataSourceContextHolder.clear();

        String token = jwtUtil.generate(username);
        tokenService.store(token, username);   // ← 新增：写入 Redis

        log.info("用户 [{}] 登录成功", username);
        return LoginResponse.builder()
                .code(200).message("登录成功")
                .token(token)
                .build();
    }

    /** 根据用户名加载完整用户信息（含权限、菜单），供 /me 和 JwtFilter 用 */
    public UserInfoResponse loadUserInfo(String username) {
        DataSourceContextHolder.set("replica");
        try {
            SysUser user = userMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
            if (user == null) return null;

            List<SysRole>       roles = roleMapper.selectByUserId(user.getId());
            List<SysPermission> perms = permMapper.selectByUserId(user.getId());

            // 树形菜单
            List<SysPermission> menus = perms.stream()
                    .filter(p -> p.getType() == 1)
                    .toList();
            List<MenuVO> menuTree = buildMenuTree(menus, null);

            return UserInfoResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .roles(roles.stream().map(SysRole::getCode).toList())
                    .permissions(perms.stream().map(SysPermission::getCode).toList())
                    .menus(menuTree)
                    .build();
        } finally {
            DataSourceContextHolder.clear();
        }
    }

    private List<MenuVO> buildMenuTree(List<SysPermission> all, String parentCode) {
        return all.stream()
                .filter(p -> Objects.equals(p.getParentCode(), parentCode))
                .map(p -> MenuVO.builder()
                        .code(p.getCode()).name(p.getName())
                        .path(p.getPath()).icon(p.getIcon())
                        .children(buildMenuTree(all, p.getCode()))
                        .build())
                .collect(Collectors.toList());
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            tokenService.revoke(header.substring(7));
        }
        return ResponseEntity.ok(Map.of("code", 200, "message", "退出成功"));
    }
}