import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { store } from '@/store';
import { refreshAccessToken, logout } from '@/store/slices/authSlice';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Add auth token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const state = store.getState();
    const token = state.auth.token;
    
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Add request ID for tracing
    config.headers['X-Request-ID'] = crypto.randomUUID();
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors and token refresh
apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };
    
    // Handle 401 Unauthorized - Attempt token refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      try {
        const result = await store.dispatch(refreshAccessToken()).unwrap();
        
        if (result.token && originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${result.token}`;
          return apiClient(originalRequest);
        }
      } catch (refreshError) {
        // Refresh failed - logout user
        store.dispatch(logout());
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    
    // Handle other errors
    const errorResponse = {
      code: error.response?.status?.toString() || 'UNKNOWN',
      message: (error.response?.data as any)?.message || error.message || 'An error occurred',
      details: (error.response?.data as any)?.details,
      timestamp: new Date().toISOString(),
      path: originalRequest?.url || '',
    };
    
    // Log error for debugging (sanitized - no sensitive data)
    console.error('API Error:', {
      status: error.response?.status,
      url: originalRequest?.url,
      method: originalRequest?.method,
      code: errorResponse.code,
    });
    
    return Promise.reject(error);
  }
);

export default apiClient;

// Helper function to mask sensitive data in logs
export const maskSensitiveData = (data: any): any => {
  if (!data) return data;
  
  const sensitiveFields = ['password', 'token', 'secret', 'cardNumber', 'cvv', 'pin', 'otp'];
  const masked = { ...data };
  
  for (const field of sensitiveFields) {
    if (masked[field]) {
      masked[field] = '***MASKED***';
    }
  }
  
  return masked;
};
