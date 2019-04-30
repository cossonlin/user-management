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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserAdminServiceTest {

    @InjectMocks
    private UserAdminService service;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    private User adminUser;
    private User managerUser;
    private User user;

    private Role adminRole;
    private Role managerRole;
    private Role userRole;

    @Before
    public void setUp() {
        userRole = new Role(RoleName.ROLE_USER);
        user = new User("user", "user@test.com", "dummyPassword");
        user.setId(3l);
        user.setRoles(Collections.singleton(userRole));

        managerRole = new Role(RoleName.ROLE_MANAGER);
        managerUser = new User("manager", "manager@test.com", "dummyPassword");
        managerUser.setId(2l);
        managerUser.setRoles(Collections.singleton(managerRole));

        adminRole = new Role(RoleName.ROLE_ADMIN);
        adminUser = new User("admin", "admin@test.com", "dummyPassword");
        adminUser.setId(1l);
        adminUser.setRoles(Collections.singleton(adminRole));

    }

    @Test(expected = ResourceNotFoundException.class)
    public void getUserInfo_ThrowResourceNotFoundException_IfIdNotExist() {
        given(userRepository.findById(anyLong())).willThrow(ResourceNotFoundException.class);
        service.getUserInfo(3l, RoleName.ROLE_USER);
    }

    @Test(expected = BadRequestException.class)
    public void getUserInfo_ThrowBadRequestException_IfDontHavePermissionToQuery() {
        given(userRepository.findById(anyLong())).willReturn(Optional.of(managerUser));
        service.getUserInfo(3l, RoleName.ROLE_USER);
    }

    @Test
    public void getUserInfo_ReturnManger_IfQueryByManager() {
        given(userRepository.findById(anyLong())).willReturn(Optional.of(managerUser));
        UserInfoResponse result = service.getUserInfo(1l, RoleName.ROLE_MANAGER);
        assertEquals(managerUser.getId(), result.getId());
        assertEquals(managerUser.getUsername(), result.getUsername());
        assertEquals(managerUser.getEmail(), result.getEmail());
    }

    @Test
    public void getUserInfo_ReturnAdmin_IfQueryByAdmin() {
        given(userRepository.findById(anyLong())).willReturn(Optional.of(adminUser));
        UserInfoResponse result = service.getUserInfo(1l, RoleName.ROLE_ADMIN);
        assertEquals(adminUser.getId(), result.getId());
        assertEquals(adminUser.getUsername(), result.getUsername());
        assertEquals(adminUser.getEmail(), result.getEmail());
    }

    @Test
    public void getAllUserByRole_ReturnAllUser_IfQueryByAdmin() {
        given(userRepository.findAll()).willReturn(Arrays.asList(user, managerUser, adminUser));
        List<UserInfoResponse> users = service.getAllUserByRole(RoleName.ROLE_ADMIN);
        assertEquals(3, users.size());
    }

    @Test
    public void getAllUserByRole_ReturnUserAndManager_IfQueryByManager() {
        given(userRepository.findAll()).willReturn(Arrays.asList(user, managerUser, adminUser));
        List<UserInfoResponse> users = service.getAllUserByRole(RoleName.ROLE_MANAGER);
        assertEquals(2, users.size());
    }

    @Test(expected = AppException.class)
    public void createUser_ThrowAppException_IfRoleNotSetup() {
        given(roleRepository.findByName(RoleName.ROLE_USER)).willThrow(AppException.class);
        RegistrationRequest request = new RegistrationRequest();
        request.setUsername("anything");
        request.setPassword("anything");
        service.createUser(request, RoleName.ROLE_USER);

    }

    @Test
    public void createUser_ReturnUserAndManager_IfQueryByManager() {
        String username = "testName";
        String email = "test@test.com";
        String password = "testPassword";
        User expectedUser = new User(username, email, password, userRole);
        given(roleRepository.findByName(RoleName.ROLE_USER)).willReturn(Optional.of(userRole));
        given(userRepository.save(any(User.class))).willReturn(expectedUser);

        RegistrationRequest request = new RegistrationRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setPassword(password);
        User actualUser = service.createUser(request, RoleName.ROLE_USER);
        assertEquals(expectedUser.getUsername(), actualUser.getUsername());
        assertEquals(expectedUser.getEmail(), actualUser.getEmail());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void updateUser_ThrowResourceNotFoundException_IfUserDoesnotExist() {
        given(userRepository.findByUsername("test")).willThrow(ResourceNotFoundException.class);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("test");
        request.setEmail("");
        service.updateUser(request, RoleName.ROLE_MANAGER);
    }

    @Test(expected = BadRequestException.class)
    public void updateUser_ThrowBadRequestException_IfDontHavePermission() {
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(adminUser));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername(adminUser.getUsername());
        request.setEmail(adminUser.getEmail());
        service.updateUser(request, RoleName.ROLE_MANAGER);
    }

    @Test
    public void updateUser_ReturnUpdatedInfo_IfUpdatedByAdmin() {
        given(userRepository.findByUsername(anyString())).willReturn(Optional.of(adminUser));

        String newEmailAddress = "update@test.com";
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername(adminUser.getUsername());
        request.setEmail(newEmailAddress);

        User updatedUser = new User(adminUser.getUsername(), newEmailAddress, adminUser.getPassword());

        given(userRepository.save(any(User.class))).willReturn(updatedUser);

        UserInfoResponse response = service.updateUser(request, RoleName.ROLE_ADMIN);

        assertEquals(newEmailAddress, response.getEmail());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void deleteUser_ThrowResourceNotFoundException_IfUserDoesnotExist() {
        given(userRepository.findById(anyLong())).willThrow(ResourceNotFoundException.class);

        service.deleteUser(4, RoleName.ROLE_MANAGER);
    }

    @Test(expected = BadRequestException.class)
    public void deleteUser_ThrowBadRequestException_IfDontHavePermission() {
        given(userRepository.findById(anyLong())).willReturn(Optional.of(adminUser));

        service.deleteUser(1, RoleName.ROLE_MANAGER);
    }

    @Test
    public void deleteUser_ReturnUpdatedInfo_IfUpdatedByAdmin() {
        given(userRepository.findById(anyLong())).willReturn(Optional.of(adminUser));
        doNothing().when(userRepository).deleteById(anyLong());

        service.deleteUser(1, RoleName.ROLE_ADMIN);
    }

    @Test
    public void getCurrentUserRole_ReturnUserRole_IfNormalUser() {
        GrantedAuthority authority = new SimpleGrantedAuthority(RoleName.ROLE_MANAGER.name());
        UserPrincipal currentUser = new UserPrincipal(1l, "username", "password", "email", Arrays.asList(authority));
        RoleName result = service.getCurrentUserRole(currentUser);
        assertEquals(RoleName.ROLE_MANAGER, result);
    }
}