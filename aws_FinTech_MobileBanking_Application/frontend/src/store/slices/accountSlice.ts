import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { accountService } from '@/services/api/accountService';
import { Account, AccountState, Transaction } from '@/types';

const initialState: AccountState = {
  accounts: [],
  selectedAccount: null,
  recentTransactions: [],
  isLoading: false,
  error: null,
};

export const fetchAccounts = createAsyncThunk(
  'accounts/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      return await accountService.getAccounts();
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch accounts');
    }
  }
);

export const fetchAccountById = createAsyncThunk(
  'accounts/fetchById',
  async (accountId: string, { rejectWithValue }) => {
    try {
      return await accountService.getAccountById(accountId);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch account');
    }
  }
);

export const fetchAccountTransactions = createAsyncThunk(
  'accounts/fetchTransactions',
  async (
    { accountId, params }: { accountId: string; params?: { page?: number; size?: number; startDate?: string; endDate?: string } },
    { rejectWithValue }
  ) => {
    try {
      return await accountService.getAccountTransactions(accountId, params);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch transactions');
    }
  }
);

export const fetchAccountStatement = createAsyncThunk(
  'accounts/fetchStatement',
  async (
    { accountId, startDate, endDate }: { accountId: string; startDate: string; endDate: string },
    { rejectWithValue }
  ) => {
    try {
      return await accountService.getAccountStatement(accountId, startDate, endDate);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch statement');
    }
  }
);

const accountSlice = createSlice({
  name: 'accounts',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setSelectedAccount: (state, action: PayloadAction<Account | null>) => {
      state.selectedAccount = action.payload;
    },
    clearSelectedAccount: (state) => {
      state.selectedAccount = null;
      state.recentTransactions = [];
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch All Accounts
      .addCase(fetchAccounts.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchAccounts.fulfilled, (state, action) => {
        state.isLoading = false;
        state.accounts = action.payload;
      })
      .addCase(fetchAccounts.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch Account By ID
      .addCase(fetchAccountById.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchAccountById.fulfilled, (state, action) => {
        state.isLoading = false;
        state.selectedAccount = action.payload;
      })
      .addCase(fetchAccountById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch Account Transactions
      .addCase(fetchAccountTransactions.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchAccountTransactions.fulfilled, (state, action) => {
        state.isLoading = false;
        state.recentTransactions = action.payload.content;
      })
      .addCase(fetchAccountTransactions.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const { clearError, setSelectedAccount, clearSelectedAccount } = accountSlice.actions;
export default accountSlice.reducer;
