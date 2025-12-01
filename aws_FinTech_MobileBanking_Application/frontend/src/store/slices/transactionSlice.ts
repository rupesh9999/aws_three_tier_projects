import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { transactionService } from '@/services/api/transactionService';
import { TransactionState, TransferRequest, TransferResponse } from '@/types';

const initialState: TransactionState = {
  transactions: [],
  pendingTransaction: null,
  transferResult: null,
  isLoading: false,
  error: null,
  totalPages: 0,
  currentPage: 0,
};

export const initiateTransfer = createAsyncThunk(
  'transactions/initiateTransfer',
  async (transferData: TransferRequest, { rejectWithValue }) => {
    try {
      return await transactionService.initiateTransfer(transferData);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Transfer failed');
    }
  }
);

export const confirmTransfer = createAsyncThunk(
  'transactions/confirmTransfer',
  async ({ transactionId, otp }: { transactionId: string; otp: string }, { rejectWithValue }) => {
    try {
      return await transactionService.confirmTransfer(transactionId, otp);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Transfer confirmation failed');
    }
  }
);

export const fetchTransactionHistory = createAsyncThunk(
  'transactions/fetchHistory',
  async (
    params: { page?: number; size?: number; accountId?: string; type?: string },
    { rejectWithValue }
  ) => {
    try {
      return await transactionService.getTransactionHistory(params);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch transactions');
    }
  }
);

export const getTransactionById = createAsyncThunk(
  'transactions/getById',
  async (transactionId: string, { rejectWithValue }) => {
    try {
      return await transactionService.getTransactionById(transactionId);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch transaction');
    }
  }
);

const transactionSlice = createSlice({
  name: 'transactions',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    clearTransferResult: (state) => {
      state.transferResult = null;
      state.pendingTransaction = null;
    },
    setPendingTransaction: (state, action) => {
      state.pendingTransaction = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      // Initiate Transfer
      .addCase(initiateTransfer.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(initiateTransfer.fulfilled, (state, action) => {
        state.isLoading = false;
        state.pendingTransaction = action.payload;
      })
      .addCase(initiateTransfer.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Confirm Transfer
      .addCase(confirmTransfer.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(confirmTransfer.fulfilled, (state, action) => {
        state.isLoading = false;
        state.transferResult = action.payload;
        state.pendingTransaction = null;
      })
      .addCase(confirmTransfer.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch Transaction History
      .addCase(fetchTransactionHistory.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchTransactionHistory.fulfilled, (state, action) => {
        state.isLoading = false;
        state.transactions = action.payload.content;
        state.totalPages = action.payload.totalPages;
        state.currentPage = action.payload.number;
      })
      .addCase(fetchTransactionHistory.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Get Transaction By ID
      .addCase(getTransactionById.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(getTransactionById.fulfilled, (state, action) => {
        state.isLoading = false;
        // Update or add to transactions list
        const index = state.transactions.findIndex(t => t.id === action.payload.id);
        if (index >= 0) {
          state.transactions[index] = action.payload;
        }
      })
      .addCase(getTransactionById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const { clearError, clearTransferResult, setPendingTransaction } = transactionSlice.actions;
export default transactionSlice.reducer;
