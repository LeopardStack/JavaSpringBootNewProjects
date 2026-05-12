package com.scnu.springbootjdk17demo.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data @Builder
public class UserInfoResponse {
    private Long   id;
    private String username;
    private String nickname;
    private List<String> roles;       // ["admin"]
    private List<String> permissions; // ["admin:dashboard", "user:add", ...]
    private List<MenuVO> menus;       // 树形菜单

    @Data @Builder
    public static class MenuVO {
        private String code;
        private String name;
        private String path;
        private String icon;
        private List<MenuVO> children;
    }
}