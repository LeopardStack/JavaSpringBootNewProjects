package com.scnu.springbootjdk17demo.security;

import com.scnu.springbootjdk17demo.dto.UserInfoResponse;
import com.scnu.springbootjdk17demo.service.TokenService;
import com.scnu.springbootjdk17demo.service.UserService;
import com.scnu.springbootjdk17demo.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil     jwtUtil;
    private final UserService userService;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            // ① JWT 签名/格式校验
            if (jwtUtil.validate(token)) {

                // ② Redis 存活校验（关键）
                String cachedUsername = tokenService.getUsername(token);
                if (cachedUsername != null) {

                    // ③ 滑动续期（可选，去掉就是固定 24h）
                    tokenService.refresh(token);

                    UserInfoResponse info = userService.loadUserInfo(cachedUsername);
                    if (info != null) {
                        List<SimpleGrantedAuthority> auths = new ArrayList<>();
                        info.getRoles().forEach(r -> auths.add(new SimpleGrantedAuthority("ROLE_" + r)));
                        info.getPermissions().forEach(p -> auths.add(new SimpleGrantedAuthority(p)));
                        var auth = new UsernamePasswordAuthenticationToken(info, null, auths);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        }
        chain.doFilter(req, resp);
    }
}