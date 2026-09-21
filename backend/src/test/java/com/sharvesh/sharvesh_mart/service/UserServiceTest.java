package com.sharvesh.sharvesh_mart.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.sharvesh.sharvesh_mart.dao.UserDao;
import com.sharvesh.sharvesh_mart.dto.RegisterRequest;
import com.sharvesh.sharvesh_mart.dto.UserResponse;
import com.sharvesh.sharvesh_mart.exception.DaoException;
import com.sharvesh.sharvesh_mart.exception.DuplicateEmailException;
import com.sharvesh.sharvesh_mart.exception.ValidationException;
import com.sharvesh.sharvesh_mart.model.Role;
import com.sharvesh.sharvesh_mart.model.User;
import com.sharvesh.sharvesh_mart.util.PasswordUtil;

/**
 * Unit tests for the authentication business rules with the DAO mocked
 * (spec Section 9 testing requirements).
 */
class UserServiceTest {

    private final UserDao userDao = mock(UserDao.class);
    private final UserService userService = new UserService(userDao);

    @Test
    void registerHashesPasswordAndStoresNormalizedEmail() throws Exception {
        when(userDao.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userDao.create(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7);
            return user;
        });

        UserResponse response = userService.register(
                new RegisterRequest(" Alice ", "Alice@Test.com", "Password123", "BUYER"));

        Assertions.assertEquals("alice@test.com", response.getEmail());
        Assertions.assertEquals(Role.BUYER, response.getRole());
        Assertions.assertEquals(7, response.getId());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).create(captor.capture());
        Assertions.assertTrue(captor.getValue().getPasswordHash().startsWith("$2a$"));
        Assertions.assertNotEquals("Password123", captor.getValue().getPasswordHash());
    }

    @Test
    void registerRejectsDuplicateEmail() throws DaoException {
        when(userDao.findByEmail("alice@test.com")).thenReturn(Optional.of(newUser("alice@test.com")));
        RegisterRequest request = new RegisterRequest("Alice", "Alice@Test.com", "Password123", "BUYER");

        Assertions.assertThrows(DuplicateEmailException.class, () -> userService.register(request));
        verify(userDao, never()).create(any(User.class));
    }

    @Test
    void registerValidatesBeforeAnyDaoCall() throws DaoException {
        RegisterRequest request = new RegisterRequest("", "bad-email", "short", "ADMIN");

        Assertions.assertThrows(ValidationException.class, () -> userService.register(request));
        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).create(any(User.class));
    }

    @Test
    void authenticateReturnsUserOnValidCredentials() throws Exception {
        when(userDao.findByEmail("buyer@test.com")).thenReturn(Optional.of(newUser("buyer@test.com")));

        Optional<User> result = userService.authenticate("BUYER@test.com", "Password123");

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("buyer@test.com", result.get().getEmail());
    }

    @Test
    void authenticateReturnsEmptyOnWrongPassword() throws Exception {
        when(userDao.findByEmail("buyer@test.com")).thenReturn(Optional.of(newUser("buyer@test.com")));

        Assertions.assertTrue(userService.authenticate("buyer@test.com", "WrongPassword1").isEmpty());
    }

    @Test
    void authenticateReturnsEmptyForUnknownEmail() throws Exception {
        when(userDao.findByEmail("nobody@test.com")).thenReturn(Optional.empty());

        Assertions.assertTrue(userService.authenticate("nobody@test.com", "Password123").isEmpty());
    }

    @Test
    void authenticateValidatesBeforeDaoCall() throws DaoException {
        Assertions.assertThrows(ValidationException.class, () -> userService.authenticate("", ""));

        verify(userDao, never()).findByEmail(anyString());
    }

    private User newUser(String email) {
        User user = new User();
        user.setId(1);
        user.setName("Test User");
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hash("Password123"));
        user.setRole(Role.BUYER);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
