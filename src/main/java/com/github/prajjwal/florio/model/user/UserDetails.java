/* Created by IntelliJ IDEA.

Author: Prajjwal Pachauri
Date: 29-08-2025
Time: 4:47 pm
File: UserDetails.java */
package com.github.prajjwal.florio.model.user;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public interface UserDetails {
    Collection<? extends GrantedAuthority> getAuthorities();

    String getUsername();

    boolean isAccountNonExpired();

    boolean isAccountNonLocked();

    boolean isCredentialNonExpired();

    boolean isEnabled();
}