package com.nimesh.repository;

import com.nimesh.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by username
     */
    User findByUsername(String username);
    
    /**
     * Find user by username (Optional version for null safety)
     */
    Optional<User> findOptionalByUsername(String username);
    
    /**
     * Check if user exists by username
     */
    boolean existsByUsername(String username);
    
    /**
     * Find users by user type/role
     */
    List<User> findByUserType(String userType);
    
    /**
     * Find all users ordered by username
     */
    List<User> findAllByOrderByUsernameAsc();
    
    /**
     * Count users by role
     */
    long countByUserType(String userType);
    
    /**
     * Find users with admin privileges
     */
    @Query("SELECT u FROM User u WHERE u.userType = 'ADMIN'")
    List<User> findAdmins();
    
    /**
     * Find active users (ordered by username)
     */
    @Query("SELECT u FROM User u ORDER BY u.username")
    List<User> findActiveUsers();
    
    /**
     * Find users by username containing (for search functionality)
     */
    List<User> findByUsernameContainingIgnoreCase(String username);
}