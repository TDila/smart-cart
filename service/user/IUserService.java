package com.vulcan.smartcart.service.user;

import com.vulcan.smartcart.dto.UserDto;
import com.vulcan.smartcart.model.User;
import com.vulcan.smartcart.request.CreateUserRequest;
import com.vulcan.smartcart.request.UserUpdateRequest;

public interface IUserService {
    User getUserById(Long userId);
    User createUser(CreateUserRequest request);
    User updateUser(UserUpdateRequest request, Long userId);
    void deleteUser(Long userId);

    UserDto convertUserToDto(User user);

    User getAuthenticatedUser();
}
