package com.inspiremymini.service;

import com.inspiremymini.api.model.UserRequest;
import com.inspiremymini.api.model.UserResponse;
import com.inspiremymini.exception.EmailAlreadyExistsException;
import com.inspiremymini.exception.UserNotFoundException;
import com.inspiremymini.exception.UsernameAlreadtExistsException;
import com.inspiremymini.mapper.UserMapper;
import com.inspiremymini.model.UserEntity;
import com.inspiremymini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::mapFromUserEntityToUserResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(()
                -> new UserNotFoundException(id));
        return userMapper.mapFromUserEntityToUserResponse(userEntity);
    }

    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyExistsException(userRequest.getEmail());
        }
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new UsernameAlreadtExistsException(userRequest.getUsername());
        }
        UserEntity userEntity = userMapper.mapFromUserRequestToUserEntity(userRequest);
        userEntity.setPassword(bCryptPasswordEncoder.encode(userEntity.getPassword()));
        UserEntity savedUserEntity = userRepository.save(userEntity);
        return userMapper.mapFromUserEntityToUserResponse(savedUserEntity);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest userRequest) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(()
                -> new UserNotFoundException(id));
        userMapper.updateUserEntityFromUserRequest(userEntity, userRequest);
        if (userRequest.getPassword() != null) {
            userEntity.setPassword(bCryptPasswordEncoder.encode(userEntity.getPassword()));
        }
        return userMapper.mapFromUserEntityToUserResponse(userRepository.save(userEntity));
    }

    @Transactional
    public void deleteUser(Long id) {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(()
                -> new UserNotFoundException(id));
        userRepository.delete(userEntity);
    }

}
