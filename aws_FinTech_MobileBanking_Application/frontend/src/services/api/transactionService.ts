import apiClient from './apiClient';
import { Transaction, TransferRequest, TransferResponse, PaginatedResponse } from '@/types';

export const transactionService = {
  initiateTransfer: async (transferData: TransferRequest): Promise<TransferResponse> => {
    const response = await apiClient.post<TransferResponse>('/transactions/transfer', transferData);
    return response.data;
  },

  confirmTransfer: async (transactionId: string, otp: string): Promise<TransferResponse> => {
    const response = await apiClient.post<TransferResponse>(`/transactions/${transactionId}/confirm`, { otp });
    return response.data;
  },

  cancelTransfer: async (transactionId: string): Promise<{ message: string }> => {
    const response = await apiClient.post(`/transactions/${transactionId}/cancel`);
    return response.data;
  },

  getTransactionHistory: async (params: {
    page?: number;
    size?: number;
    accountId?: string;
    type?: string;
    startDate?: string;
    endDate?: string;
    status?: string;
  }): Promise<PaginatedResponse<Transaction>> => {
    const response = await apiClient.get<PaginatedResponse<Transaction>>('/transactions', { params });
    return response.data;
  },

  getTransactionById: async (transactionId: string): Promise<Transaction> => {
    const response = await apiClient.get<Transaction>(`/transactions/${transactionId}`);
    return response.data;
  },

  searchTransactions: async (query: string, params?: {
    page?: number;
    size?: number;
  }): Promise<PaginatedResponse<Transaction>> => {
    const response = await apiClient.get<PaginatedResponse<Transaction>>('/transactions/search', {
      params: { query, ...params },
    });
    return response.data;
  },

  getTransactionReceipt: async (transactionId: string): Promise<Blob> => {
    const response = await apiClient.get(`/transactions/${transactionId}/receipt`, {
      responseType: 'blob',
    });
    return response.data;
  },

  getTransferLimits: async (accountId: string): Promise<{
    dailyLimit: number;
    usedDailyLimit: number;
    perTransactionLimit: number;
    neftLimit: number;
    rtgsLimit: number;
    impsLimit: number;
  }> => {
    const response = await apiClient.get(`/transactions/limits/${accountId}`);
    return response.data;
  },

  validateIfsc: async (ifscCode: string): Promise<{
    valid: boolean;
    bankName?: string;
    branchName?: string;
    address?: string;
  }> => {
    const response = await apiClient.get(`/transactions/validate-ifsc/${ifscCode}`);
    return response.data;
  },
};
