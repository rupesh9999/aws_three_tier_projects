package com.streamflix.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users", schema = "auth")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Column(name = "first_name", length = 100)
    private String firstName;
    
    @Column(name = "last_name", length = 100)
    private String lastName;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;
    
    @Column(name = "phone_verified")
    @Builder.Default
    private Boolean phoneVerified = false;
    
    @Column(name = "account_locked")
    @Builder.Default
    private Boolean accountLocked = false;
    
    @Column(name = "lock_reason", length = 500)
    private String lockReason;
    
    @Column(name = "locked_at")
    private LocalDateTime lockedAt;
    
    @Column(name = "lock_expires_at")
    private LocalDateTime lockExpiresAt;
    
    @Column(name = "failed_login_attempts")
    @Builder.Default
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_login_ip", length = 45)
    private String lastLoginIp;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "subscription_plan", length = 50)
    @Builder.Default
    private String subscriptionPlan = "BASIC";
    
    @Column(name = "subscription_status", length = 50)
    @Builder.Default
    private String subscriptionStatus = "ACTIVE";
    
    @Column(name = "subscription_expires_at")
    private LocalDateTime subscriptionExpiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Version
    private Long version;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        schema = "auth",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<RefreshToken> refreshTokens = new HashSet<>();
    
    // Helper methods
    public void addRole(Role role) {
        roles.add(role);
    }
    
    public void removeRole(Role role) {
        roles.remove(role);
    }
    
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(r -> r.getName().equals(roleName));
    }
    
    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }
    
    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }
    
    public void lock(String reason, int durationMinutes) {
        this.accountLocked = true;
        this.lockReason = reason;
        this.lockedAt = LocalDateTime.now();
        this.lockExpiresAt = LocalDateTime.now().plusMinutes(durationMinutes);
    }
    
    public void unlock() {
        this.accountLocked = false;
        this.lockReason = null;
        this.lockedAt = null;
        this.lockExpiresAt = null;
        this.failedLoginAttempts = 0;
    }
    
    public boolean isLockExpired() {
        return lockExpiresAt != null && LocalDateTime.now().isAfter(lockExpiresAt);
    }
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
    
    public String getFullName() {
        return (firstName != null ? firstName : "") + 
               (lastName != null ? " " + lastName : "");
    }
}
