package com.bekasi.complaint.security;

import com.bekasi.complaint.entity.User;
import com.bekasi.complaint.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService, UserDetailsPasswordService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return UserDetailsImpl.build(user);
    }

    @Override
    public UserDetails updatePassword(UserDetails userDetails, @Nullable String newPassword) {
        if (newPassword == null) {
            throw new IllegalArgumentException("newPassword must not be null");
        }

        // Spring Security may call this method to persist a newly encoded password
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userDetails.getUsername()));

        user.setPassword(newPassword);
        User updated = userRepository.save(user);

        return UserDetailsImpl.build(updated);
    }
}
