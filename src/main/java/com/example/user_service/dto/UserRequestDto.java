package com.example.user_service.dto;

import lombok.Data;

@Data
public class UserRequestDto {

    private String email;
    private String fullName;
    private String phone;
}
