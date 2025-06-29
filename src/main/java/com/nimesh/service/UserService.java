package com.nimesh.service;

import com.nimesh.model.User;
import com.nimesh.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Find all users
     */
    public List<User> findAllUsers() {
        return userRepository.findAllByOrderByUsernameAsc();
    }

    /**
     * Find user by ID
     */
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by username
     */
    public Optional<User> findUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        return Optional.ofNullable(user);
    }

    /**
     * Check if user exists by username
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Save a new user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Update an existing user
     */
    public User updateUser(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("User ID cannot be null for update operation");
        }
        return userRepository.save(user);
    }

    /**
     * Delete user by ID
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User with ID " + id + " does not exist");
        }
        userRepository.deleteById(id);
    }

    /**
     * Reset user password
     */
    public void resetPassword(Long userId, String newPassword) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            throw new IllegalArgumentException("User with ID " + userId + " does not exist");
        }
    }

    /**
     * Authenticate user
     */
    public boolean authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return passwordEncoder.matches(rawPassword, user.getPassword());
        }
        return false;
    }

    /**
     * Create default admin user if no users exist
     */
    public void createDefaultAdminIfNotExists() {
        if (userRepository.count() == 0) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setUserType("ADMIN");
            userRepository.save(adminUser);
        }
    }

    /**
     * Change user password
     */
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    /**
     * Get users by role
     */
    public List<User> findUsersByRole(String role) {
        return userRepository.findByUserType(role);
    }

    /**
     * Count total users
     */
    public long countUsers() {
        return userRepository.count();
    }

    /**
     * Check if user has admin privileges
     */
    public boolean isAdmin(String username) {
        User user = userRepository.findByUsername(username);
        return user != null && user.isAdmin();
    }

    /**
     * Check if user has manager privileges
     */
    public boolean isManager(String username) {
        User user = userRepository.findByUsername(username);
        return user != null && (user.isAdmin() || user.isManager());
    }

    /**
     * Search users by username
     */
    public List<User> searchUsersByUsername(String searchTerm) {
        return userRepository.findByUsernameContainingIgnoreCase(searchTerm);
    }

    /**
     * Get user by username (non-Optional version for compatibility)
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Count users by type
     */
    public long countByUserType(String userType) {
        return userRepository.countByUserType(userType);
    }

    /**
     * Check if user can be deleted (prevent deletion of last admin)
     */
    public boolean canDeleteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isAdmin()) {
                // Count admin users
                long adminCount = userRepository.countByUserType("ADMIN");
                return adminCount > 1; // Can delete if there's more than one admin
            }
            return true; // Non-admin users can always be deleted
        }
        return false;
    }
}