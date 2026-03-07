package com.minal.taskflow.services;

import com.minal.taskflow.exceptions.UserNotFoundException;
import com.minal.taskflow.models.UserModel;
import com.minal.taskflow.repositories.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        UserModel user = userRepository.findByUserName(userName)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return User.withUsername(user.getUserName())
                .password(user.getPassword())
                .authorities("ROLE_" + user.getRole())
                .build();
    }
}
