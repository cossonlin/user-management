package com.cosson.usermanagement.controller;

import com.cosson.usermanagement.dto.ApiResponse;
import com.cosson.usermanagement.dto.RegistrationRequest;
import com.cosson.usermanagement.entity.RoleName;
import com.cosson.usermanagement.entity.User;
import com.cosson.usermanagement.service.UserAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserAdminService service;

    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (service.hasUsernameExist(registrationRequest.getUsername())) {
            return new ResponseEntity(new ApiResponse(false, "Username is already taken!"),
                    HttpStatus.BAD_REQUEST);
        }

        service.createUser(registrationRequest, RoleName.ROLE_ADMIN);

        return ResponseEntity.ok(new ApiResponse(true, "Admin user has been created successfully"));
    }
}
