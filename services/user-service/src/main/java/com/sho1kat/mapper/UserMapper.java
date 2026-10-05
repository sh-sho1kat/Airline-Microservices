package com.sho1kat.mapper;

import com.sho1kat.entity.User;
import com.sho1kat.payload.dto.UserDto;

import java.util.List;

public class UserMapper {
    public static UserDto toDto(User user) {
        if(user == null) {
            return null;
        }
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userRoles(user.getRoles())
                .build();
    }
    public static List<UserDto> toDto(List<User> users) {
        return users.stream().map(UserMapper::toDto).toList();
    }
}
