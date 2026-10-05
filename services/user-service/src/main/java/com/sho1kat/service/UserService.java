package com.sho1kat.service;

import com.sho1kat.entity.User;
import com.sho1kat.payload.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto getUserById(Long id);
    UserDto getUserByEmail(String email);
    List<UserDto> getAllUsers();
}
