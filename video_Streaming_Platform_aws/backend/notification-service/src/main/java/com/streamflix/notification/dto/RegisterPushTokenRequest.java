package com.streamflix.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterPushTokenRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
    
    @NotBlank(message = "Device type is required")
    private String deviceType; // IOS, ANDROID, WEB
    
    @NotBlank(message = "Device ID is required")
    private String deviceId;
    
    private String deviceName;
    
    private String appVersion;
}
