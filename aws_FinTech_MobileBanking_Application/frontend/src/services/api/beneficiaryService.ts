import apiClient from './apiClient';
import { Beneficiary, AddBeneficiaryRequest } from '@/types';

export const beneficiaryService = {
  getBeneficiaries: async (): Promise<Beneficiary[]> => {
    const response = await apiClient.get<Beneficiary[]>('/beneficiaries');
    return response.data;
  },

  getBeneficiaryById: async (beneficiaryId: string): Promise<Beneficiary> => {
    const response = await apiClient.get<Beneficiary>(`/beneficiaries/${beneficiaryId}`);
    return response.data;
  },

  addBeneficiary: async (data: AddBeneficiaryRequest): Promise<Beneficiary> => {
    const response = await apiClient.post<Beneficiary>('/beneficiaries', data);
    return response.data;
  },

  verifyBeneficiary: async (beneficiaryId: string, otp: string): Promise<Beneficiary> => {
    const response = await apiClient.post<Beneficiary>(`/beneficiaries/${beneficiaryId}/verify`, { otp });
    return response.data;
  },

  deleteBeneficiary: async (beneficiaryId: string): Promise<void> => {
    await apiClient.delete(`/beneficiaries/${beneficiaryId}`);
  },

  updateBeneficiary: async (
    beneficiaryId: string,
    data: Partial<AddBeneficiaryRequest>
  ): Promise<Beneficiary> => {
    const response = await apiClient.put<Beneficiary>(`/beneficiaries/${beneficiaryId}`, data);
    return response.data;
  },

  updateTransferLimit: async (
    beneficiaryId: string,
    limit: number
  ): Promise<Beneficiary> => {
    const response = await apiClient.patch<Beneficiary>(`/beneficiaries/${beneficiaryId}/limit`, { limit });
    return response.data;
  },

  resendVerificationOtp: async (beneficiaryId: string): Promise<{ message: string }> => {
    const response = await apiClient.post(`/beneficiaries/${beneficiaryId}/resend-otp`);
    return response.data;
  },
};
