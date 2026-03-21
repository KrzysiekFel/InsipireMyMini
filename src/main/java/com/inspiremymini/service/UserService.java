package com.inspiremymini.service;

import com.inspiremymini.dto.UserRequest;
import com.inspiremymini.dto.UserResponse;
import com.inspiremymini.exception.UserNotFoundException;
import com.inspiremymini.mapper.UserMapper;
import com.inspiremymini.model.UserEntity;
import com.inspiremymini.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
        userRepository.deleteById(id);
    }

}

// TODO sprawdzić w update czy nie ma już update albo czy save nie wystarczy
//      -> nie ma, można tak jak zrobiłem albo jeszcze org.mapstruct @Mapper
// TODO tranzakcje wprowadzic
//      -> zrobione
// TODO dodać bcrypt, bean bcreaptencoder
//      -> zrobione
// TODO rzucamy customowy exception np UserNotFoundException, Unchecked ... extends Runtime
//      -> zrobione
// TODO przygotować swoje exception
//      -> zrobione


