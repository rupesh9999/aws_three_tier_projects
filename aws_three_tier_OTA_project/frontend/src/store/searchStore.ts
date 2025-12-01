import { create } from 'zustand';
import type { FlightSearchParams, HotelSearchParams, TrainSearchParams, BusSearchParams } from '@/types';

type SearchType = 'flights' | 'hotels' | 'trains' | 'buses';

interface SearchState {
  activeSearchType: SearchType;
  flightSearch: FlightSearchParams | null;
  hotelSearch: HotelSearchParams | null;
  trainSearch: TrainSearchParams | null;
  busSearch: BusSearchParams | null;
  recentSearches: Array<{
    type: SearchType;
    params: FlightSearchParams | HotelSearchParams | TrainSearchParams | BusSearchParams;
    timestamp: number;
  }>;
  setActiveSearchType: (type: SearchType) => void;
  setFlightSearch: (params: FlightSearchParams) => void;
  setHotelSearch: (params: HotelSearchParams) => void;
  setTrainSearch: (params: TrainSearchParams) => void;
  setBusSearch: (params: BusSearchParams) => void;
  addRecentSearch: (type: SearchType, params: FlightSearchParams | HotelSearchParams | TrainSearchParams | BusSearchParams) => void;
  clearRecentSearches: () => void;
}

export const useSearchStore = create<SearchState>()((set) => ({
  activeSearchType: 'flights',
  flightSearch: null,
  hotelSearch: null,
  trainSearch: null,
  busSearch: null,
  recentSearches: [],

  setActiveSearchType: (type) => set({ activeSearchType: type }),

  setFlightSearch: (params) =>
    set((state) => {
      state.addRecentSearch('flights', params);
      return { flightSearch: params };
    }),

  setHotelSearch: (params) =>
    set((state) => {
      state.addRecentSearch('hotels', params);
      return { hotelSearch: params };
    }),

  setTrainSearch: (params) =>
    set((state) => {
      state.addRecentSearch('trains', params);
      return { trainSearch: params };
    }),

  setBusSearch: (params) =>
    set((state) => {
      state.addRecentSearch('buses', params);
      return { busSearch: params };
    }),

  addRecentSearch: (type, params) =>
    set((state) => ({
      recentSearches: [
        { type, params, timestamp: Date.now() },
        ...state.recentSearches.slice(0, 9),
      ],
    })),

  clearRecentSearches: () => set({ recentSearches: [] }),
}));
