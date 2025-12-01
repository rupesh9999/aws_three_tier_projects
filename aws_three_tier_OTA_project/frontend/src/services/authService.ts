import { apiClient, handleApiError } from './api';
import type {
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  User,
  Traveler,
  ApiResponse,
} from '@/types';

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', credentials);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', data);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async logout(): Promise<void> {
    try {
      await apiClient.post('/auth/logout');
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async refreshToken(refreshToken: string): Promise<AuthResponse> {
    try {
      const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/refresh', {
        refreshToken,
      });
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getCurrentUser(): Promise<User> {
    try {
      const response = await apiClient.get<ApiResponse<User>>('/auth/me');
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async updateProfile(data: Partial<User>): Promise<User> {
    try {
      const response = await apiClient.put<ApiResponse<User>>('/auth/profile', data);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async changePassword(currentPassword: string, newPassword: string): Promise<void> {
    try {
      await apiClient.post('/auth/change-password', {
        currentPassword,
        newPassword,
      });
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getSavedTravelers(): Promise<Traveler[]> {
    try {
      const response = await apiClient.get<ApiResponse<Traveler[]>>('/travelers');
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async addTraveler(traveler: Omit<Traveler, 'id' | 'userId'>): Promise<Traveler> {
    try {
      const response = await apiClient.post<ApiResponse<Traveler>>('/travelers', traveler);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async updateTraveler(id: string, traveler: Partial<Traveler>): Promise<Traveler> {
    try {
      const response = await apiClient.put<ApiResponse<Traveler>>(`/travelers/${id}`, traveler);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async deleteTraveler(id: string): Promise<void> {
    try {
      await apiClient.delete(`/travelers/${id}`);
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
