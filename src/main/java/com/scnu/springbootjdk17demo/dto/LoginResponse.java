package com.scnu.springbootjdk17demo.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Integer code;
    private String  message;
    private String  token;
    private String  username;
    private String  nickname;
}