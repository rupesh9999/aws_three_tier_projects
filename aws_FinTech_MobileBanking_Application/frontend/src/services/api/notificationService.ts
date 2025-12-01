import apiClient from './apiClient';
import { Notification, PaginatedResponse } from '@/types';

export const notificationService = {
  getNotifications: async (params?: {
    page?: number;
    size?: number;
    type?: string;
    read?: boolean;
  }): Promise<PaginatedResponse<Notification>> => {
    const response = await apiClient.get<PaginatedResponse<Notification>>('/notifications', { params });
    return response.data;
  },

  getNotificationById: async (notificationId: string): Promise<Notification> => {
    const response = await apiClient.get<Notification>(`/notifications/${notificationId}`);
    return response.data;
  },

  markAsRead: async (notificationId: string): Promise<Notification> => {
    const response = await apiClient.patch<Notification>(`/notifications/${notificationId}/read`);
    return response.data;
  },

  markAllAsRead: async (): Promise<{ message: string }> => {
    const response = await apiClient.patch('/notifications/read-all');
    return response.data;
  },

  deleteNotification: async (notificationId: string): Promise<void> => {
    await apiClient.delete(`/notifications/${notificationId}`);
  },

  getUnreadCount: async (): Promise<{ count: number }> => {
    const response = await apiClient.get('/notifications/unread-count');
    return response.data;
  },

  updatePreferences: async (preferences: {
    transactionAlerts: boolean;
    securityAlerts: boolean;
    promotionalAlerts: boolean;
    emailNotifications: boolean;
    smsNotifications: boolean;
    pushNotifications: boolean;
  }): Promise<{ message: string }> => {
    const response = await apiClient.put('/notifications/preferences', preferences);
    return response.data;
  },

  getPreferences: async (): Promise<{
    transactionAlerts: boolean;
    securityAlerts: boolean;
    promotionalAlerts: boolean;
    emailNotifications: boolean;
    smsNotifications: boolean;
    pushNotifications: boolean;
  }> => {
    const response = await apiClient.get('/notifications/preferences');
    return response.data;
  },
};
