package com.example.user_service.kafka.consumer;


import com.example.user_service.dto.UserRequestDto;
import com.example.user_service.kafka.event.UserCreatedEvent;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserKafkaListener {

    private final UserService userService;

    @KafkaListener(topics = "user-created", groupId = "user-service-group")
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Received user-created event: {}", event);

        userService.createUser(toUserRequestDto(event));
    }

    private UserRequestDto toUserRequestDto(UserCreatedEvent event) {
        return UserRequestDto.builder()
                .authUserId(event.getAuthUserId())
                .email(event.getEmail())
                .phone(event.getPhone())
                .fullName(event.getFullName())
                .role(event.getRole())
                .build();
    }
}
