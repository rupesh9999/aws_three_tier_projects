import { apiClient, handleApiError } from './api';
import type { Cart, CartItem, AddOn, ApiResponse } from '@/types';

export const cartService = {
  async getCart(): Promise<Cart> {
    try {
      const response = await apiClient.get<ApiResponse<Cart>>('/cart');
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async addToCart(item: Omit<CartItem, 'id'>): Promise<Cart> {
    try {
      const response = await apiClient.post<ApiResponse<Cart>>('/cart/items', item);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async updateCartItem(itemId: string, updates: Partial<CartItem>): Promise<Cart> {
    try {
      const response = await apiClient.put<ApiResponse<Cart>>(`/cart/items/${itemId}`, updates);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async removeFromCart(itemId: string): Promise<Cart> {
    try {
      const response = await apiClient.delete<ApiResponse<Cart>>(`/cart/items/${itemId}`);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async clearCart(): Promise<void> {
    try {
      await apiClient.delete('/cart');
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async addAddOn(itemId: string, addOn: AddOn): Promise<Cart> {
    try {
      const response = await apiClient.post<ApiResponse<Cart>>(
        `/cart/items/${itemId}/addons`,
        addOn
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async removeAddOn(itemId: string, addOnId: string): Promise<Cart> {
    try {
      const response = await apiClient.delete<ApiResponse<Cart>>(
        `/cart/items/${itemId}/addons/${addOnId}`
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getAvailableAddOns(itemType: string, itemId: string): Promise<AddOn[]> {
    try {
      const response = await apiClient.get<ApiResponse<AddOn[]>>('/cart/addons', {
        params: { type: itemType, itemId },
      });
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async validateCart(): Promise<{ valid: boolean; errors: string[] }> {
    try {
      const response = await apiClient.post<ApiResponse<{ valid: boolean; errors: string[] }>>(
        '/cart/validate'
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getCartSummary(): Promise<{
    itemCount: number;
    subtotal: number;
    taxes: number;
    fees: number;
    total: number;
    currency: string;
  }> {
    try {
      const response = await apiClient.get<
        ApiResponse<{
          itemCount: number;
          subtotal: number;
          taxes: number;
          fees: number;
          total: number;
          currency: string;
        }>
      >('/cart/summary');
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
