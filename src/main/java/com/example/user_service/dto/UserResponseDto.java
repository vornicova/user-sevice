package com.example.user_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDto {

    private Long id;
    private String email;
    private String fullName;
    private String phone;
}
