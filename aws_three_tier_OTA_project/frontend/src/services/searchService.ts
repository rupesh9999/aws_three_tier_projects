import { apiClient, handleApiError } from './api';
import type {
  FlightSearchParams,
  HotelSearchParams,
  TrainSearchParams,
  BusSearchParams,
  Flight,
  Hotel,
  Train,
  Bus,
  Room,
  ApiResponse,
  PaginatedResponse,
} from '@/types';

export const searchService = {
  // Flight Search
  async searchFlights(params: FlightSearchParams): Promise<PaginatedResponse<Flight>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Flight>>>('/flights/search', {
        params,
      });
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getFlightDetails(id: string): Promise<Flight> {
    try {
      const response = await apiClient.get<ApiResponse<Flight>>(`/flights/${id}`);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getFlightAvailability(id: string, date: string): Promise<{ available: boolean; seats: number }> {
    try {
      const response = await apiClient.get<ApiResponse<{ available: boolean; seats: number }>>(
        `/flights/${id}/availability`,
        { params: { date } }
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  // Hotel Search
  async searchHotels(params: HotelSearchParams): Promise<PaginatedResponse<Hotel>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Hotel>>>('/hotels/search', {
        params,
      });
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getHotelDetails(id: string): Promise<Hotel> {
    try {
      const response = await apiClient.get<ApiResponse<Hotel>>(`/hotels/${id}`);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getHotelRooms(hotelId: string, checkIn: string, checkOut: string): Promise<Room[]> {
    try {
      const response = await apiClient.get<ApiResponse<Room[]>>(`/hotels/${hotelId}/rooms`, {
        params: { checkIn, checkOut },
      });
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  // Train Search
  async searchTrains(params: TrainSearchParams): Promise<PaginatedResponse<Train>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Train>>>('/trains/search', {
        params,
      });
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getTrainDetails(id: string): Promise<Train> {
    try {
      const response = await apiClient.get<ApiResponse<Train>>(`/trains/${id}`);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  // Bus Search
  async searchBuses(params: BusSearchParams): Promise<PaginatedResponse<Bus>> {
    try {
      const response = await apiClient.get<ApiResponse<PaginatedResponse<Bus>>>('/buses/search', {
        params,
      });
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getBusDetails(id: string): Promise<Bus> {
    try {
      const response = await apiClient.get<ApiResponse<Bus>>(`/buses/${id}`);
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  // Common
  async getPopularDestinations(): Promise<{ city: string; country: string; image: string }[]> {
    try {
      const response = await apiClient.get<ApiResponse<{ city: string; country: string; image: string }[]>>(
        '/search/popular-destinations'
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getAirports(query: string): Promise<{ code: string; name: string; city: string }[]> {
    try {
      const response = await apiClient.get<ApiResponse<{ code: string; name: string; city: string }[]>>(
        '/search/airports',
        { params: { q: query } }
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },

  async getCities(query: string): Promise<{ name: string; country: string }[]> {
    try {
      const response = await apiClient.get<ApiResponse<{ name: string; country: string }[]>>(
        '/search/cities',
        { params: { q: query } }
      );
      return response.data.data;
    } catch (error) {
      throw handleApiError(error);
    }
  },
};
