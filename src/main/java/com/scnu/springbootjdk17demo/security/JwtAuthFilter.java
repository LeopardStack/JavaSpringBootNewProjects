package com.scnu.springbootjdk17demo.security;

import com.scnu.springbootjdk17demo.dto.UserInfoResponse;
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

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validate(token)) {
                String username = jwtUtil.parseUsername(token);
                UserInfoResponse info = userService.loadUserInfo(username);
                if (info != null) {
                    // 把 ROLE_xxx 和 权限码 都注入 SecurityContext
                    List<SimpleGrantedAuthority> auths = new ArrayList<>();
                    info.getRoles().forEach(r -> auths.add(new SimpleGrantedAuthority("ROLE_" + r)));
                    info.getPermissions().forEach(p -> auths.add(new SimpleGrantedAuthority(p)));

                    var auth = new UsernamePasswordAuthenticationToken(info, null, auths);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(req, resp);
    }
}