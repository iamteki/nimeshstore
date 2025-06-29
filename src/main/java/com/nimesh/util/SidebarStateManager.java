package com.nimesh.util;

import org.springframework.stereotype.Component;

/**
 * Utility class to manage and persist sidebar collapsed/expanded state
 * across different screens in the application.
 */
@Component
public class SidebarStateManager {
    
    private boolean sidebarCollapsed = false;
    
    /**
     * Sets the sidebar collapsed state
     * 
     * @param collapsed true if the sidebar is collapsed, false if expanded
     */
    public void setSidebarCollapsed(boolean collapsed) {
        this.sidebarCollapsed = collapsed;
    }
    
    /**
     * Gets the current sidebar collapsed state
     * 
     * @return true if the sidebar is collapsed, false if expanded
     */
    public boolean isSidebarCollapsed() {
        return sidebarCollapsed;
    }
    
    /**
     * Toggles the sidebar state between collapsed and expanded
     * 
     * @return the new state after toggling (true if now collapsed, false if now expanded)
     */
    public boolean toggleSidebarState() {
        sidebarCollapsed = !sidebarCollapsed;
        return sidebarCollapsed;
    }
}