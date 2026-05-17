package com.inspiremymini.service;

import com.inspiremymini.api.model.UserRequest;
import com.inspiremymini.api.model.UserResponse;
import com.inspiremymini.exception.EmailAlreadyExistsException;
import com.inspiremymini.exception.UserNotFoundException;
import com.inspiremymini.exception.UsernameAlreadtExistsException;
import com.inspiremymini.mapper.UserMapper;
import com.inspiremymini.model.UserEntity;
import com.inspiremymini.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void getAllUsersShouldReturnAllMappedUsers() {
        // given
        UserEntity userEntity1 = new UserEntity(
                1L, "username1", "password1", "user1@example.com");
        UserEntity userEntity2 = new UserEntity(
                2L, "username2", "password2", "user2@example.com");
        UserResponse userResponse1 = new UserResponse()
                .id(1L)
                .username("username1")
                .email("user1@example.com");
        UserResponse userResponse2 = new UserResponse()
                .id(1L)
                .username("username1")
                .email("user1@example.com");
        when(userRepository.findAll()).thenReturn(List.of(userEntity1, userEntity2));
        when(userMapper.mapFromUserEntityToUserResponse(userEntity1)).thenReturn(userResponse1);
        when(userMapper.mapFromUserEntityToUserResponse(userEntity2)).thenReturn(userResponse2);

        // when
        List<UserResponse> result = userService.getAllUsers();

        // then
        assertNotNull(result);
        assertEquals(List.of(userResponse1, userResponse2), result);
        verify(userRepository, times(1)).findAll();
        verify(userMapper, times(2)).mapFromUserEntityToUserResponse(any(UserEntity.class));
    }

    @Test
    public void getUserByIdShouldReturnUserResponseDataWhenUserExists() {
        // given
        UserEntity userEntity = new UserEntity(
                1L, "username", "password", "user@example.com");
        UserResponse userResponse = new UserResponse()
                .id(1L)
                .username("username")
                .email("user@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userMapper.mapFromUserEntityToUserResponse(userEntity)).thenReturn(userResponse);

        // when
        UserResponse result = userService.getUserById(1L);

        // then
        assertEquals(userResponse, result);
        verify(userRepository).findById(1L);
        verify(userMapper).mapFromUserEntityToUserResponse(userEntity);
    }

    @Test
    public void getUserByIdShouldThrowExceptionWhenUserNotFound() {
        // given
        Long notExistingUserId = 999L;
        when(userRepository.findById(notExistingUserId)).thenReturn(Optional.empty());

        // when
        UserNotFoundException result = assertThrows(
                UserNotFoundException.class, () -> userService.getUserById(notExistingUserId));

        // then
        assertEquals("User with id: 999 not found.", result.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
    }

    @Test
    public void createUserShouldCreateUserWhenNotExists() {
        // given
        UserRequest userRequest = new UserRequest(
                "username", "password", "user@example.com");
        UserEntity userEntity = new UserEntity(
                1L, "username", "password", "user@example.com");
        UserEntity savedEntity = new UserEntity(
                1L, "username", "encodedPassword", "user@example.com");
        UserResponse userResponse = new UserResponse()
                .id(1L)
                .username("username")
                .email("user@example.com");
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userRequest.getUsername())).thenReturn(false);
        when(userMapper.mapFromUserRequestToUserEntity(userRequest)).thenReturn(userEntity);
        when(bCryptPasswordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(userEntity)).thenReturn(savedEntity);
        when(userMapper.mapFromUserEntityToUserResponse(savedEntity)).thenReturn(userResponse);

        // when
        UserResponse result = userService.createUser(userRequest);

        // then
        assertEquals(userResponse, result);
    }

    @Test
    public void createUserShouldThrowExceptionWhenAssociatedEmailAlreadyExists() {
        // given
        UserRequest userRequest = new UserRequest(
                "username", "password", "user@example.com");
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        // when
        EmailAlreadyExistsException result = assertThrows(
                EmailAlreadyExistsException.class, () -> userService.createUser(userRequest));

        // then
        assertEquals("User with email: user@example.com already exist.", result.getMessage());
        assertEquals(HttpStatus.CONFLICT, result.getHttpStatus());
    }

    @Test
    public void createUserShouldThrowExceptionWhenExistByUsername() {
        // given
        UserRequest userRequest = new UserRequest(
                "username", "password", "user@example.com");
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(userRequest.getUsername())).thenReturn(true);

        // when
        UsernameAlreadtExistsException result = assertThrows(
                UsernameAlreadtExistsException.class, () -> userService.createUser(userRequest));

        // then
        assertEquals("Username: username already exist.", result.getMessage());
        assertEquals(HttpStatus.CONFLICT, result.getHttpStatus());
        verify(userRepository).existsByEmail(userRequest.getEmail());
        verify(userRepository).existsByUsername(userRequest.getUsername());
    }

    @Test
    public void updateUserShouldUpdateEntityWhenUserExist() {
        //given
        Long existingId = 1L;
        UserRequest userRequest = new UserRequest(
                "usernameNew", "passwordNew", "userNew@example.com");
        UserEntity existingUserEntity = new UserEntity(
                1L, "username", "password", "user@example.com");
        UserEntity updatedUserEntity = new UserEntity(
                1L, "usernameNew", "encodedPassword", "userNew@example.com");
        UserResponse userResponse = new UserResponse()
                .id(1L)
                .username("usernameNew")
                .email("userNew@example.com");
        when(userRepository.findById(existingId)).thenReturn(Optional.of(existingUserEntity));
        doAnswer(invocation -> {
            UserEntity entity = invocation.getArgument(0);
            UserRequest request = invocation.getArgument(1);

            entity.setUsername(request.getUsername());
            entity.setEmail(request.getEmail());
            entity.setPassword(request.getPassword());

            return null;
        }).when(userMapper).updateUserEntityFromUserRequest(existingUserEntity, userRequest);
        when(bCryptPasswordEncoder.encode("passwordNew"))
                .thenReturn("encodedPassword");
        when(userRepository.save(existingUserEntity))
                .thenReturn(updatedUserEntity);
        when(userMapper.mapFromUserEntityToUserResponse(updatedUserEntity))
                .thenReturn(userResponse);

        // when
        UserResponse result = userService.updateUser(existingId, userRequest);

        // then
        assertEquals(userResponse, result);
        assertAll(
                () -> assertEquals("usernameNew", existingUserEntity.getUsername()),
                () -> assertEquals("encodedPassword", existingUserEntity.getPassword())
        );
        verify(userRepository).findById(existingId);
        verify(userRepository).save(existingUserEntity);
    }

    @Test
    public void updateUserShouldUThrowExceptionWhenUserDoesNotExist() {
        // given
        Long notExistingUserId = 999L;
        UserRequest userRequest = new UserRequest(
                "usernameNew", "passwordNew", "userNew@example.com");
        when(userRepository.findById(notExistingUserId)).thenReturn(Optional.empty());

        // when
        UserNotFoundException result = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(notExistingUserId, userRequest));

        // then
        assertEquals("User with id: 999 not found.", result.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, result.getHttpStatus());
        verify(userRepository).findById(notExistingUserId);
    }

    @Test
    void deleteUserWhenIdExists() {
        // given
        Long existingId = 1L;
        UserEntity userEntity = new UserEntity(
                1L, "username", "password", "user@example.com");
        when(userRepository.findById(existingId))
                .thenReturn(Optional.of(userEntity));

        // when
        userService.deleteUser(existingId);

        // then
        verify(userRepository).findById(existingId);
        verify(userRepository).delete(userEntity);
    }

    @Test
    public void deleteUserReturnExceptionWhenIdNotExists() {
        // given
        Long notExistingUserId = 999L;
        when(userRepository.findById(notExistingUserId)).thenReturn(Optional.empty());

        // when
        UserNotFoundException result = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUser(notExistingUserId));

        // then
        assertEquals("User with id: 999 not found.", result.getMessage());
        verify(userRepository).findById(notExistingUserId);
        verify(userRepository, never()).delete(any());
    }
}
