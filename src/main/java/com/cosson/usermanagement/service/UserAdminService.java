package com.cosson.usermanagement.service;

import com.cosson.usermanagement.dto.RegistrationRequest;
import com.cosson.usermanagement.dto.UserInfoResponse;
import com.cosson.usermanagement.dto.UserUpdateRequest;
import com.cosson.usermanagement.entity.Role;
import com.cosson.usermanagement.entity.RoleName;
import com.cosson.usermanagement.entity.User;
import com.cosson.usermanagement.exception.AppException;
import com.cosson.usermanagement.exception.BadRequestException;
import com.cosson.usermanagement.exception.ResourceNotFoundException;
import com.cosson.usermanagement.repo.RoleRepository;
import com.cosson.usermanagement.repo.UserRepository;
import com.cosson.usermanagement.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserInfoResponse getUserInfo(long id, RoleName roleName) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User", "id", id)
        );
        if (hasPermission(user.getRoles(), roleName)) {
            return new UserInfoResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles());
        } else {
            throw new BadRequestException("Don't have permission to do this");
        }
    }

    public List<UserInfoResponse> getAllUserByRole(RoleName operatorRoleName) {
        return userRepository.findAll().stream()
                .filter(user -> hasPermission(user.getRoles(), operatorRoleName))
                .map(user -> new UserInfoResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles()))
                .collect(Collectors.toList());
    }

    public boolean hasUsernameExist(String username) {
        return userRepository.existsByUsername(username);
    }

    public User createUser(RegistrationRequest registrationRequest, RoleName assignRoleName) {
        User user = new User(registrationRequest.getUsername(), registrationRequest.getEmail(), registrationRequest.getPassword());
        Role userRole = roleRepository.findByName(assignRoleName)
                .orElseThrow(() -> new AppException("User Role not set."));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Collections.singleton(userRole));
        return userRepository.save(user);
    }

    public UserInfoResponse updateUser(UserUpdateRequest user, RoleName operatorRole) {
        User currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(() ->
                new ResourceNotFoundException("User", "username", user.getUsername())
        );
        if (hasPermission(currentUser.getRoles(), operatorRole)) {
            if (user.getEmail() != null) {
                currentUser.setEmail(user.getEmail());
            }
            User updatedUser = userRepository.save(currentUser);
            return convertUserToUserInfo(updatedUser);
        } else {
            throw new BadRequestException("Don't have permission to do this");
        }
    }

    public void deleteUser(long id, RoleName operatorRole) {
        User currentUser = userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("User", "id", id)
        );
        if (hasPermission(currentUser.getRoles(), operatorRole)) {
            userRepository.deleteById(id);
        } else {
            throw new BadRequestException("Don't have permission to do this");
        }
    }

    public RoleName getCurrentUserRole(UserPrincipal currentUser) {
        List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>) currentUser.getAuthorities();
        return RoleName.valueOf(authorities.get(0).getAuthority());
    }

    private UserInfoResponse convertUserToUserInfo(User updatedUser) {
        return new UserInfoResponse(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail(), updatedUser.getRoles());
    }

    private boolean hasPermission(Set<Role> roleSet, RoleName operatorRole) {
        for (Role role : roleSet) {
            if (role.getName().getValue() < operatorRole.getValue()) {
                return false;
            }
        }
        return true;
    }
}
