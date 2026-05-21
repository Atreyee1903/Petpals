package org.petpals.security;

import org.petpals.model.User;
import org.petpals.repository.BlockedUserRepository;
import org.petpals.repository.UserRepository;
import org.petpals.service.UserBlockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BlockedUserRepository blockedUserRepository;

    public CustomUserDetailsService(UserRepository userRepository, BlockedUserRepository blockedUserRepository) {
        this.userRepository = userRepository;
        this.blockedUserRepository = blockedUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        if (user.isBlocked() || blockedUserRepository.existsByUserIdAndActiveTrue(user.getId())) {
            throw new UserBlockedException("Your account has been blocked. Please contact support.", user.getId());
        }
        
        return new CustomUserDetails(user);
    }
}


