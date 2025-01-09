package com.busanit501.bootproject.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    // 필요한 경우 추가 필드
}
