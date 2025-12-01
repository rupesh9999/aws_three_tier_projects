import apiClient from './apiClient';
import { Account, Transaction, PaginatedResponse } from '@/types';

export const accountService = {
  getAccounts: async (): Promise<Account[]> => {
    const response = await apiClient.get<Account[]>('/accounts');
    return response.data;
  },

  getAccountById: async (accountId: string): Promise<Account> => {
    const response = await apiClient.get<Account>(`/accounts/${accountId}`);
    return response.data;
  },

  getAccountTransactions: async (
    accountId: string,
    params?: {
      page?: number;
      size?: number;
      startDate?: string;
      endDate?: string;
      type?: string;
    }
  ): Promise<PaginatedResponse<Transaction>> => {
    const response = await apiClient.get<PaginatedResponse<Transaction>>(
      `/accounts/${accountId}/transactions`,
      { params }
    );
    return response.data;
  },

  getAccountStatement: async (
    accountId: string,
    startDate: string,
    endDate: string
  ): Promise<Blob> => {
    const response = await apiClient.get(`/accounts/${accountId}/statement`, {
      params: { startDate, endDate },
      responseType: 'blob',
    });
    return response.data;
  },

  getAccountBalance: async (accountId: string): Promise<{ balance: number; availableBalance: number }> => {
    const response = await apiClient.get(`/accounts/${accountId}/balance`);
    return response.data;
  },

  getMiniStatement: async (accountId: string): Promise<Transaction[]> => {
    const response = await apiClient.get<Transaction[]>(`/accounts/${accountId}/mini-statement`);
    return response.data;
  },

  getAccountSummary: async (): Promise<{
    totalBalance: number;
    totalAccounts: number;
    savingsAccounts: number;
    currentAccounts: number;
  }> => {
    const response = await apiClient.get('/accounts/summary');
    return response.data;
  },
};
