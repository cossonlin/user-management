package com.cosson.usermanagement.controller;

import com.cosson.usermanagement.dto.ApiResponse;
import com.cosson.usermanagement.dto.RegistrationRequest;
import com.cosson.usermanagement.dto.UserInfoResponse;
import com.cosson.usermanagement.dto.UserUpdateRequest;
import com.cosson.usermanagement.entity.RoleName;
import com.cosson.usermanagement.security.UserPrincipal;
import com.cosson.usermanagement.service.UserAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('USER','MANAGER','ADMIN')")
public class UserController {

    @Autowired
    private UserAdminService service;

    @GetMapping("/me")
    public UserInfoResponse getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserInfoResponse userInfoResponse = new UserInfoResponse(currentUser.getId(), currentUser.getUsername(), currentUser.getEmail(), currentUser.getAuthorities());
        return userInfoResponse;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        if (id == currentUser.getId()) {
            return ResponseEntity.ok(new UserInfoResponse(currentUser.getId(), currentUser.getUsername(), currentUser.getEmail(), currentUser.getAuthorities()));
        }
        RoleName currentUserRole = service.getCurrentUserRole(currentUser);
        if (currentUserRole.equals(RoleName.ROLE_USER)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "You can query your own profile only!"));
        } else {
            return ResponseEntity.ok(service.getUserInfo(id, currentUserRole));
        }
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<?> getAllUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        RoleName roleName = service.getCurrentUserRole(currentUser);
        if (roleName.equals(RoleName.ROLE_USER)) {
            return ResponseEntity.ok(new UserInfoResponse(currentUser.getId(), currentUser.getUsername(), currentUser.getEmail(), currentUser.getAuthorities()));
        } else {
            return ResponseEntity.ok(service.getAllUserByRole(roleName));
        }
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (service.hasUsernameExist(registrationRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Username is already taken!"));
        }

        service.createUser(registrationRequest, RoleName.ROLE_USER);

        return ResponseEntity.ok(new ApiResponse(true, "User has been created successfully"));
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal UserPrincipal currentUser, @RequestBody UserUpdateRequest updateRequest) {
        RoleName currentUserRole = service.getCurrentUserRole(currentUser);
        if (Objects.equals(RoleName.ROLE_USER, currentUserRole)
                && !Objects.equals(currentUser.getUsername(), updateRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "You can update your own profile only!"));
        }
        return ResponseEntity.ok(service.updateUser(updateRequest, currentUserRole));
    }

    /*
     * User can only delete themselves profile
     * Manager can delete all managers' and users' profile
     * Admin can delete anyone's profile
     * */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        RoleName currentUserRole = service.getCurrentUserRole(currentUser);
        if (Objects.equals(RoleName.ROLE_USER, currentUserRole) && id != currentUser.getId()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "You can't delete other user's profile!"));
        }
        service.deleteUser(id, currentUserRole);
        return ResponseEntity.ok(new ApiResponse(true, "User has been deleted successfully"));
    }
}
