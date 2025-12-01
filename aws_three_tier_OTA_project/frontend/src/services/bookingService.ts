import { apiClient, handleApiError } from './api';
import type {
  Booking,
  Traveler,
  PaymentRequest,
  PaymentResponse,
  ApiResponse,
  PaginatedResponse,
} from '@/types';

export const bookingService = {
  async createBooking(travelers: Traveler[]): Promise<Booking> {
    try {
      const response = await apiClient.post<ApiResponse<Booking>>('/bookings', { travelers });
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getBooking(id: string): Promise<Booking> {
    try {
      const response = await apiClient.get<ApiResponse<Booking>>(`/bookings/${id}`);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getBookingByReference(reference: string): Promise<Booking> {
    try {
      const response = await apiClient.get<ApiResponse<Booking>>(`/bookings/reference/${reference}`);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getUserBookings(params?: {
    status?: string;
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<Booking>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Booking>>>('/bookings', {
        params,
      });
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getUpcomingBookings(): Promise<Booking[]> {
    try {
      const response = await apiClient.get<ApiResponse<Booking[]>>('/bookings/upcoming');
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getPastBookings(page = 0, size = 10): Promise<PaginatedResponse<Booking>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Booking>>>(
        '/bookings/past',
        { params: { page, size } }
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async cancelBooking(id: string, reason: string): Promise<Booking> {
    try {
      const response = await apiClient.post<ApiResponse<Booking>>(`/bookings/${id}/cancel`, {
        reason,
      });
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getETicket(bookingId: string): Promise<Blob> {
    try {
      const response = await apiClient.get(`/bookings/${bookingId}/e-ticket`, {
        responseType: 'blob',
      });
      return response.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async processPayment(paymentRequest: PaymentRequest): Promise<PaymentResponse> {
    try {
      const response = await apiClient.post<ApiResponse<PaymentResponse>>(
        '/payments/process',
        paymentRequest
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getPaymentStatus(paymentId: string): Promise<PaymentResponse> {
    try {
      const response = await apiClient.get<ApiResponse<PaymentResponse>>(
        `/payments/${paymentId}/status`
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async requestRefund(bookingId: string, reason: string): Promise<{ refundId: string; status: string }> {
    try {
      const response = await apiClient.post<ApiResponse<{ refundId: string; status: string }>>(
        `/bookings/${bookingId}/refund`,
        { reason }
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getRefundStatus(refundId: string): Promise<{
    status: string;
    amount: number;
    processedAt?: string;
  }> {
    try {
      const response = await apiClient.get<
        ApiResponse<{ status: string; amount: number; processedAt?: string }>
      >(`/refunds/${refundId}`);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
