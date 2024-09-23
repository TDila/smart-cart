package com.vulcan.smartcart.service.user;

import com.vulcan.smartcart.dto.UserDto;
import com.vulcan.smartcart.exceptions.AlreadyExistsException;
import com.vulcan.smartcart.exceptions.ResourceNotFoundException;
import com.vulcan.smartcart.model.User;
import com.vulcan.smartcart.repository.UserRepository;
import com.vulcan.smartcart.request.CreateUserRequest;
import com.vulcan.smartcart.request.UserUpdateRequest;
import com.vulcan.smartcart.service.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    @Override
    public User getUserById(Long userId) {
        logger.info("Fetching user with ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User with ID: {} not found", userId);
                    return new ResourceNotFoundException("User not found!");
                });
    }

    @Override
    public User createUser(CreateUserRequest request) {
        logger.info("Creating user with email: {}", request.getEmail());
        return Optional.of(request)
                .filter(user -> !userRepository.existsByEmail(request.getEmail()))
                .map(req -> {
                    User user = new User();
                    user.setEmail(request.getEmail());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());
                    User savedUser = userRepository.save(user);
                    logger.info("User with email: {} successfully created", request.getEmail());
                    return savedUser;
                })
                .orElseThrow(() -> {
                    logger.error("User creation failed: email {} already exists", request.getEmail());
                    return new AlreadyExistsException("Oops! " + request.getEmail() + " already exists!");
                });
    }

    @Override
    public User updateUser(UserUpdateRequest request, Long userId) {
        logger.info("Updating user with ID: {}", userId);
        return userRepository.findById(userId).map(existingUser -> {
            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            User updatedUser = userRepository.save(existingUser);
            logger.info("User with ID: {} updated successfully", userId);
            return updatedUser;
        }).orElseThrow(() -> {
            logger.error("User with ID: {} not found for update", userId);
            return new ResourceNotFoundException("User not found!");
        });
    }

    @Override
    public void deleteUser(Long userId) {
        logger.info("Deleting user with ID: {}", userId);
        userRepository.findById(userId).ifPresentOrElse(user -> {
            userRepository.delete(user);
            logger.info("User with ID: {} deleted successfully", userId);
        }, () -> {
            logger.error("User with ID: {} not found for deletion", userId);
            throw new ResourceNotFoundException("User not found!");
        });
    }

    @Override
    public UserDto convertUserToDto(User user) {
        logger.info("Converting user with ID: {} to UserDto", user.getId());
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        logger.info("Fetching authenticated user by email: {}", email);
        return userRepository.findByEmail(email);
    }

}
