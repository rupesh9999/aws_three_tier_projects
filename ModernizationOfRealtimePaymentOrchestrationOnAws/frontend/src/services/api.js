import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || '/api/v1';

const api = axios.create({
    baseURL: API_BASE,
    timeout: 10000,
    headers: { 'Content-Type': 'application/json' },
});

export const paymentApi = {
    initiatePayment: (data) => api.post('/payments', data),
    getPayment: (id) => api.get(`/payments/${id}`),
    getPaymentsByTenant: (tenantId, page = 0, size = 20) =>
        api.get(`/payments/tenant/${tenantId}`, { params: { page, size } }),
    healthCheck: () => api.get('/payments/health'),
};

export default api;
