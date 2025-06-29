package com.nimesh.util;

import com.nimesh.model.User;

/**
 * Singleton class to maintain the current user session throughout the application
 */
public class SessionManager {
    
    private static SessionManager instance;
    private User currentUser;
    
    private SessionManager() {
        // Private constructor to enforce singleton pattern
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    public boolean isAdmin() {
        return currentUser != null && "ADMIN".equals(currentUser.getUserType());
    }
    
    public boolean isEmployee() {
        return currentUser != null && "EMPLOYEE".equals(currentUser.getUserType());
    }
    
    public String getCurrentUserType() {
        return currentUser != null ? currentUser.getUserType() : null;
    }
    
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "Unknown User";
    }
    
    public void clearSession() {
        currentUser = null;
    }
}