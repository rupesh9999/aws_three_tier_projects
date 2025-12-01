import apiClient from './apiClient';
import { LoginRequest, RegisterRequest, AuthResponse, User } from '@/types';

export const authService = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/login', credentials);
    return response.data;
  },

  register: async (userData: RegisterRequest): Promise<{ message: string }> => {
    const response = await apiClient.post('/auth/register', userData);
    return response.data;
  },

  verifyMfa: async (otp: string, mfaToken: string): Promise<AuthResponse> => {
    const response = await apiClient.post<AuthResponse>('/auth/verify-mfa', {
      otp,
      mfaToken,
    });
    return response.data;
  },

  refreshToken: async (refreshToken: string): Promise<{ token: string }> => {
    const response = await apiClient.post('/auth/refresh', { refreshToken });
    return response.data;
  },

  logout: async (): Promise<void> => {
    try {
      await apiClient.post('/auth/logout');
    } catch (error) {
      // Ignore errors on logout
    }
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get<User>('/auth/me');
    return response.data;
  },

  forgotPassword: async (email: string): Promise<{ message: string }> => {
    const response = await apiClient.post('/auth/forgot-password', { email });
    return response.data;
  },

  resetPassword: async (token: string, password: string): Promise<{ message: string }> => {
    const response = await apiClient.post('/auth/reset-password', { token, password });
    return response.data;
  },

  changePassword: async (currentPassword: string, newPassword: string): Promise<{ message: string }> => {
    const response = await apiClient.post('/auth/change-password', {
      currentPassword,
      newPassword,
    });
    return response.data;
  },

  enableMfa: async (): Promise<{ qrCode: string; secret: string }> => {
    const response = await apiClient.post('/auth/mfa/enable');
    return response.data;
  },

  disableMfa: async (otp: string): Promise<{ message: string }> => {
    const response = await apiClient.post('/auth/mfa/disable', { otp });
    return response.data;
  },

  resendOtp: async (mfaToken: string): Promise<{ message: string }> => {
    const response = await apiClient.post('/auth/resend-otp', { mfaToken });
    return response.data;
  },
};
