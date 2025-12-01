package com.streamflix.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * User principal containing authenticated user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {
    
    private UUID userId;
    private String email;
    private UUID currentProfileId;
    private String subscriptionPlan;
    private List<String> roles;
    private boolean emailVerified;
    private boolean accountLocked;
    
    /**
     * Check if user has a specific role
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * Check if user is an admin
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
    
    /**
     * Check if user has premium subscription
     */
    public boolean isPremium() {
        return "PREMIUM".equals(subscriptionPlan) || hasRole("ROLE_PREMIUM");
    }
    
    /**
     * Check if user is a content manager
     */
    public boolean isContentManager() {
        return hasRole("ROLE_CONTENT_MANAGER") || isAdmin();
    }
}
