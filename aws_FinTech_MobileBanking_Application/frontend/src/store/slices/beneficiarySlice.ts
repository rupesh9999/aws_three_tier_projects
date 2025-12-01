import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { beneficiaryService } from '@/services/api/beneficiaryService';
import { Beneficiary, BeneficiaryState, AddBeneficiaryRequest } from '@/types';

const initialState: BeneficiaryState = {
  beneficiaries: [],
  selectedBeneficiary: null,
  isLoading: false,
  error: null,
};

export const fetchBeneficiaries = createAsyncThunk(
  'beneficiaries/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      return await beneficiaryService.getBeneficiaries();
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch beneficiaries');
    }
  }
);

export const addBeneficiary = createAsyncThunk(
  'beneficiaries/add',
  async (data: AddBeneficiaryRequest, { rejectWithValue }) => {
    try {
      return await beneficiaryService.addBeneficiary(data);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to add beneficiary');
    }
  }
);

export const verifyBeneficiary = createAsyncThunk(
  'beneficiaries/verify',
  async ({ beneficiaryId, otp }: { beneficiaryId: string; otp: string }, { rejectWithValue }) => {
    try {
      return await beneficiaryService.verifyBeneficiary(beneficiaryId, otp);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Verification failed');
    }
  }
);

export const deleteBeneficiary = createAsyncThunk(
  'beneficiaries/delete',
  async (beneficiaryId: string, { rejectWithValue }) => {
    try {
      await beneficiaryService.deleteBeneficiary(beneficiaryId);
      return beneficiaryId;
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to delete beneficiary');
    }
  }
);

const beneficiarySlice = createSlice({
  name: 'beneficiaries',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setSelectedBeneficiary: (state, action) => {
      state.selectedBeneficiary = action.payload;
    },
    clearSelectedBeneficiary: (state) => {
      state.selectedBeneficiary = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch Beneficiaries
      .addCase(fetchBeneficiaries.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchBeneficiaries.fulfilled, (state, action) => {
        state.isLoading = false;
        state.beneficiaries = action.payload;
      })
      .addCase(fetchBeneficiaries.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Add Beneficiary
      .addCase(addBeneficiary.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(addBeneficiary.fulfilled, (state, action) => {
        state.isLoading = false;
        state.beneficiaries.push(action.payload);
      })
      .addCase(addBeneficiary.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Verify Beneficiary
      .addCase(verifyBeneficiary.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(verifyBeneficiary.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.beneficiaries.findIndex(b => b.id === action.payload.id);
        if (index >= 0) {
          state.beneficiaries[index] = action.payload;
        }
      })
      .addCase(verifyBeneficiary.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Delete Beneficiary
      .addCase(deleteBeneficiary.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(deleteBeneficiary.fulfilled, (state, action) => {
        state.isLoading = false;
        state.beneficiaries = state.beneficiaries.filter(b => b.id !== action.payload);
      })
      .addCase(deleteBeneficiary.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const { clearError, setSelectedBeneficiary, clearSelectedBeneficiary } = beneficiarySlice.actions;
export default beneficiarySlice.reducer;
