package com.campusgate.service;

import com.campusgate.dto.UserDTO;
import com.campusgate.entity.AccountStatus;
import com.campusgate.entity.Role;
import com.campusgate.entity.User;
import com.campusgate.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers_shouldMapPageOfUsersToDtos() {
        User user = User.builder()
                .id(1L)
                .fullName("Dana")
                .email("dana@example.com")
                .role(Role.ADMIN)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<UserDTO> result = userService.getAllUsers(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Dana", result.getContent().get(0).getFullName());
        assertEquals(Role.ADMIN, result.getContent().get(0).getRole());
    }

    @Test
    void getUserById_shouldThrowWhenUserIsMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.getUserById(99L));
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void updateStatus_shouldPersistAndReturnUpdatedUser() {
        User user = User.builder()
                .id(2L)
                .fullName("Eli")
                .email("eli@example.com")
                .role(Role.SECURITY)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDTO result = userService.updateStatus(2L, AccountStatus.SUSPENDED);

        assertEquals(AccountStatus.SUSPENDED, result.getAccountStatus());
        assertEquals(AccountStatus.SUSPENDED, user.getAccountStatus());
        verify(userRepository).save(user);
    }
}
