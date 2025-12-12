package com.example.user_service.service.impl;

import com.example.user_service.dto.UserRequestDto;
import com.example.user_service.dto.UserResponseDto;
import com.example.user_service.entity.User;
import com.example.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_shouldSaveNewUser_whenEmailNotExists() {
        // given
        UserRequestDto request = new UserRequestDto();
        request.setEmail("test@example.com");
        request.setFullName("Test User");
        request.setPhone("+123456789");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);

        User saved = User.builder()
                .id(1L)
                .email("test@example.com")
                .fullName("Test User")
                .phone("+123456789")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(saved);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // when
        UserResponseDto dto = userService.createUser(request);

        // then
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(userCaptor.capture());

        User toSave = userCaptor.getValue();
        assertEquals("test@example.com", toSave.getEmail());
        assertEquals("Test User", toSave.getFullName());
        assertEquals("+123456789", toSave.getPhone());
        assertNotNull(toSave.getCreatedAt());
        assertNotNull(toSave.getUpdatedAt());

        assertEquals(1L, dto.getId());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("Test User", dto.getFullName());
        assertEquals("+123456789", dto.getPhone());
    }

    @Test
    void createUser_shouldThrow_whenEmailAlreadyExists() {
        // given
        UserRequestDto request = new UserRequestDto();
        request.setEmail("exists@example.com");

        when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

        // when / then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.createUser(request)
        );

        assertTrue(ex.getMessage().contains("User with email already exists"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_shouldReturnDto_whenUserExists() {
        // given
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .fullName("User Name")
                .phone("+111111111")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        UserResponseDto dto = userService.getUserById(1L);

        // then
        assertEquals(1L, dto.getId());
        assertEquals("user@example.com", dto.getEmail());
        assertEquals("User Name", dto.getFullName());
        assertEquals("+111111111", dto.getPhone());
    }

    @Test
    void getUserById_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.getUserById(999L)
        );

        assertTrue(ex.getMessage().contains("User not found: 999"));
    }

    @Test
    void getAllUsers_shouldMapListToDtos() {
        User u1 = User.builder()
                .id(1L)
                .email("u1@example.com")
                .fullName("U1")
                .phone("1")
                .build();

        User u2 = User.builder()
                .id(2L)
                .email("u2@example.com")
                .fullName("U2")
                .phone("2")
                .build();

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        // when
        List<UserResponseDto> dtos = userService.getAllUsers();

        // then
        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(2L, dtos.get(1).getId());
    }

    @Test
    void updateUser_shouldUpdateFieldsAndSave_whenUserExists() {
        // given
        User existing = User.builder()
                .id(1L)
                .email("old@example.com")
                .fullName("Old Name")
                .phone("000")
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        UserRequestDto request = new UserRequestDto();
        request.setFullName("New Name");
        request.setPhone("123456");

        User saved = User.builder()
                .id(1L)
                .email("old@example.com") // email не меняется
                .fullName("New Name")
                .phone("123456")
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.save(existing)).thenReturn(saved);

        // when
        UserResponseDto dto = userService.updateUser(1L, request);

        // then
        assertEquals("New Name", existing.getFullName());
        assertEquals("123456", existing.getPhone());
        assertNotNull(existing.getUpdatedAt());

        assertEquals(1L, dto.getId());
        assertEquals("old@example.com", dto.getEmail());
        assertEquals("New Name", dto.getFullName());
        assertEquals("123456", dto.getPhone());
    }

    @Test
    void updateUser_shouldThrow_whenUserNotFound() {
        when(userRepository.findById(777L)).thenReturn(Optional.empty());

        UserRequestDto request = new UserRequestDto();
        request.setFullName("Name");
        request.setPhone("123");

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(777L, request)
        );

        assertTrue(ex.getMessage().contains("User not found: 777"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_shouldCallRepositoryDelete() {
        // when
        userService.deleteUser(5L);

        // then
        verify(userRepository).deleteById(5L);
    }
}