package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.otus.hw.models.Role;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService uds;

    @Test
    @DisplayName("loadUserByUsername returns user with authorities")
    void loadOk() {
        var user = createUser("testUser", "password", true, Set.of(Role.ROLE_USER));
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        var userDetails = uds.loadUserByUsername("testUser");

        assertThat(userDetails.getUsername()).isEqualTo("testUser");
        assertThat(userDetails.getPassword()).isEqualTo("password");
        assertThat(userDetails.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        verify(userRepository).findByUsername("testUser");
    }

    @Test
    @DisplayName("loadUserByUsername returns user with multiple authorities")
    void loadWithMultipleRoles() {
        var user = createUser("admin", "admin123", true, Set.of(Role.ROLE_USER, Role.ROLE_ADMIN));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        var userDetails = uds.loadUserByUsername("admin");

        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("loadUserByUsername returns disabled user when enabled is false")
    void loadDisabledUser() {
        var user = createUser("disabled", "pass", false, Set.of(Role.ROLE_USER));
        when(userRepository.findByUsername("disabled")).thenReturn(Optional.of(user));

        var userDetails = uds.loadUserByUsername("disabled");

        assertThat(userDetails.getUsername()).isEqualTo("disabled");
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("loadUserByUsername throws for missing user")
    void loadMissing() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> uds.loadUserByUsername("absent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("absent")
                .hasMessageContaining("not found");

        verify(userRepository).findByUsername("absent");
    }

    private User createUser(String username, String password, boolean enabled, Set<Role> roles) {
        var user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword(password);
        user.setEnabled(enabled);
        user.setRoles(roles);
        return user;
    }

}
