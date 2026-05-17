package com.inspiremymini.mapper;

import com.inspiremymini.api.model.UserRequest;
import com.inspiremymini.api.model.UserResponse;
import com.inspiremymini.model.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse mapFromUserEntityToUserResponse(UserEntity userEntity) {
        return new UserResponse()
                .id(userEntity.getId())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail());
    }

    public UserEntity mapFromUserRequestToUserEntity(UserRequest userRequest) {
        return UserEntity.builder()
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .email(userRequest.getEmail())
                .build();
    }

    public void updateUserEntityFromUserRequest(UserEntity userEntity, UserRequest userRequest) {
        if (userRequest.getUsername() != null) {
            userEntity.setUsername(userRequest.getUsername());
        }
        if (userRequest.getEmail() != null) {
            userEntity.setEmail(userRequest.getEmail());
        }
        if (userRequest.getPassword() != null) {
            userEntity.setPassword(userRequest.getPassword());
        }
    }
}
