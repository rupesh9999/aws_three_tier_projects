import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { cardService } from '@/services/api/cardService';
import { Card, CardState, CardLimitUpdate } from '@/types';

const initialState: CardState = {
  cards: [],
  selectedCard: null,
  isLoading: false,
  error: null,
};

export const fetchCards = createAsyncThunk(
  'cards/fetchAll',
  async (_, { rejectWithValue }) => {
    try {
      return await cardService.getCards();
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch cards');
    }
  }
);

export const fetchCardById = createAsyncThunk(
  'cards/fetchById',
  async (cardId: string, { rejectWithValue }) => {
    try {
      return await cardService.getCardById(cardId);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to fetch card');
    }
  }
);

export const lockCard = createAsyncThunk(
  'cards/lock',
  async (cardId: string, { rejectWithValue }) => {
    try {
      return await cardService.lockCard(cardId);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to lock card');
    }
  }
);

export const unlockCard = createAsyncThunk(
  'cards/unlock',
  async (cardId: string, { rejectWithValue }) => {
    try {
      return await cardService.unlockCard(cardId);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to unlock card');
    }
  }
);

export const updateCardLimits = createAsyncThunk(
  'cards/updateLimits',
  async ({ cardId, limits }: { cardId: string; limits: CardLimitUpdate }, { rejectWithValue }) => {
    try {
      return await cardService.updateLimits(cardId, limits);
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || 'Failed to update limits');
    }
  }
);

const cardSlice = createSlice({
  name: 'cards',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    },
    setSelectedCard: (state, action) => {
      state.selectedCard = action.payload;
    },
    clearSelectedCard: (state) => {
      state.selectedCard = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Fetch Cards
      .addCase(fetchCards.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchCards.fulfilled, (state, action) => {
        state.isLoading = false;
        state.cards = action.payload;
      })
      .addCase(fetchCards.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Fetch Card By ID
      .addCase(fetchCardById.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(fetchCardById.fulfilled, (state, action) => {
        state.isLoading = false;
        state.selectedCard = action.payload;
      })
      .addCase(fetchCardById.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Lock Card
      .addCase(lockCard.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(lockCard.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.cards.findIndex(c => c.id === action.payload.id);
        if (index >= 0) {
          state.cards[index] = action.payload;
        }
        if (state.selectedCard?.id === action.payload.id) {
          state.selectedCard = action.payload;
        }
      })
      .addCase(lockCard.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Unlock Card
      .addCase(unlockCard.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(unlockCard.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.cards.findIndex(c => c.id === action.payload.id);
        if (index >= 0) {
          state.cards[index] = action.payload;
        }
        if (state.selectedCard?.id === action.payload.id) {
          state.selectedCard = action.payload;
        }
      })
      .addCase(unlockCard.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      // Update Limits
      .addCase(updateCardLimits.pending, (state) => {
        state.isLoading = true;
        state.error = null;
      })
      .addCase(updateCardLimits.fulfilled, (state, action) => {
        state.isLoading = false;
        const index = state.cards.findIndex(c => c.id === action.payload.id);
        if (index >= 0) {
          state.cards[index] = action.payload;
        }
        if (state.selectedCard?.id === action.payload.id) {
          state.selectedCard = action.payload;
        }
      })
      .addCase(updateCardLimits.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const { clearError, setSelectedCard, clearSelectedCard } = cardSlice.actions;
export default cardSlice.reducer;
