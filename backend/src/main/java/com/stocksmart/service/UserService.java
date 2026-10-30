package com.stocksmart.service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stocksmart.dto.CreateUserRequest;
import com.stocksmart.dto.UpdateUserRequest;
import com.stocksmart.dto.UserResponse;
import com.stocksmart.entity.Role;
import com.stocksmart.entity.RoleName;
import com.stocksmart.entity.User;
import com.stocksmart.exception.BusinessException;
import com.stocksmart.exception.ConflictException;
import com.stocksmart.exception.ResourceNotFoundException;
import com.stocksmart.repository.RoleRepository;
import com.stocksmart.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> list() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(this::toUserResponse)
                .toList();
    }

    public UserResponse get(Long id) {
        return toUserResponse(findUser(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.email().trim().toLowerCase())) {
            throw new ConflictException("Email is already registered");
        }

        User user = new User();
        user.setUsername(request.username().trim());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setActive(true);
        user.setRoles(Set.of(resolveRole(request.role())));
        userRepository.save(user);
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request, String currentUsername) {
        User user = findUser(id);
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new ConflictException("Email is already registered");
        }

        boolean nextActive = Boolean.TRUE.equals(request.active());
        preventSelfDeactivation(user, currentUsername, nextActive);
        preventRemovingLastAdmin(user, request.role(), nextActive);

        user.setEmail(email);
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setActive(nextActive);
        user.setRoles(Set.of(resolveRole(request.role())));
        return toUserResponse(user);
    }

    @Transactional
    public void deactivate(Long id, String currentUsername) {
        User user = findUser(id);
        preventSelfDeactivation(user, currentUsername, false);
        preventRemovingLastAdmin(user, currentRole(user), false);
        user.setActive(false);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Role resolveRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException(roleName + " role is not seeded"));
    }

    private void preventSelfDeactivation(User user, String currentUsername, boolean nextActive) {
        if (!nextActive && user.getUsername().equals(currentUsername)) {
            throw new BusinessException("You cannot deactivate your own account");
        }
    }

    private void preventRemovingLastAdmin(User user, RoleName nextRole, boolean nextActive) {
        boolean currentlyActiveAdmin = user.isActive() && currentRole(user) == RoleName.ADMIN;
        boolean willRemainActiveAdmin = nextActive && nextRole == RoleName.ADMIN;
        if (currentlyActiveAdmin && !willRemainActiveAdmin
                && userRepository.countByRoles_NameAndActiveTrue(RoleName.ADMIN) <= 1) {
            throw new BusinessException("Cannot remove or deactivate the last active admin");
        }
    }

    private RoleName currentRole(User user) {
        return user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse(RoleName.STAFF);
    }

    private UserResponse toUserResponse(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .sorted()
                .toList();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                roles);
    }
}
