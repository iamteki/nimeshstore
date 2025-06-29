package com.nimesh.service;

import com.nimesh.model.User;
import com.nimesh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class LoginService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostConstruct
    public void init() {
        // Create default users if they don't exist
        if (userRepository.findByUsername("admin") == null) {
            User adminUser = new User("admin", passwordEncoder.encode("admin123"), "ADMIN");
            userRepository.save(adminUser);
        }
        if (userRepository.findByUsername("employee") == null) {
            User employeeUser = new User("employee", passwordEncoder.encode("employee123"), "EMPLOYEE");
            userRepository.save(employeeUser);
        }
    }
    
    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);
        return user != null && passwordEncoder.matches(password, user.getPassword());
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}