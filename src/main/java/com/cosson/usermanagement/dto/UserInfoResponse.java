package com.cosson.usermanagement.dto;

import com.cosson.usermanagement.entity.Role;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String roles;

    public UserInfoResponse(Long id, String username, String email, Set<Role> roleSet) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roleSet.stream().map(role -> role.getName().name()).collect(Collectors.joining(","));
    }

    public UserInfoResponse(Long id, String username, String email, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = authorities.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    public Long getId() {
      return id;
    }

    public void setId(Long id) {
      this.id = id;
    }

    public String getUsername() {
      return username;
    }

    public void setUsername(String username) {
      this.username = username;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getRoles() {
      return roles;
    }

    public void setRoles(String roles) {
      this.roles = roles;
    }
}
