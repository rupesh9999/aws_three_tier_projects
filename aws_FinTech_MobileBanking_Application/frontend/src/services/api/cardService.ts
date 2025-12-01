import apiClient from './apiClient';
import { Card, CardLimitUpdate, Transaction, PaginatedResponse } from '@/types';

export const cardService = {
  getCards: async (): Promise<Card[]> => {
    const response = await apiClient.get<Card[]>('/cards');
    return response.data;
  },

  getCardById: async (cardId: string): Promise<Card> => {
    const response = await apiClient.get<Card>(`/cards/${cardId}`);
    return response.data;
  },

  lockCard: async (cardId: string): Promise<Card> => {
    const response = await apiClient.post<Card>(`/cards/${cardId}/lock`);
    return response.data;
  },

  unlockCard: async (cardId: string): Promise<Card> => {
    const response = await apiClient.post<Card>(`/cards/${cardId}/unlock`);
    return response.data;
  },

  updateLimits: async (cardId: string, limits: CardLimitUpdate): Promise<Card> => {
    const response = await apiClient.patch<Card>(`/cards/${cardId}/limits`, limits);
    return response.data;
  },

  getCardTransactions: async (
    cardId: string,
    params?: { page?: number; size?: number; startDate?: string; endDate?: string }
  ): Promise<PaginatedResponse<Transaction>> => {
    const response = await apiClient.get<PaginatedResponse<Transaction>>(
      `/cards/${cardId}/transactions`,
      { params }
    );
    return response.data;
  },

  blockCard: async (cardId: string, reason: string): Promise<Card> => {
    const response = await apiClient.post<Card>(`/cards/${cardId}/block`, { reason });
    return response.data;
  },

  requestNewCard: async (data: {
    cardType: 'DEBIT' | 'CREDIT';
    linkedAccountId: string;
  }): Promise<{ message: string; requestId: string }> => {
    const response = await apiClient.post('/cards/request', data);
    return response.data;
  },

  changePin: async (cardId: string, data: {
    currentPin: string;
    newPin: string;
    confirmPin: string;
  }): Promise<{ message: string }> => {
    const response = await apiClient.post(`/cards/${cardId}/change-pin`, data);
    return response.data;
  },

  generateVirtualCard: async (accountId: string): Promise<Card> => {
    const response = await apiClient.post<Card>('/cards/virtual', { accountId });
    return response.data;
  },
};
