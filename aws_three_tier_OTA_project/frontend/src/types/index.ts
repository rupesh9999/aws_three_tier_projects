// User Types
export interface User {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  avatar?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Traveler {
  id: string;
  userId: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  gender: 'MALE' | 'FEMALE' | 'OTHER';
  passportNumber?: string;
  passportExpiry?: string;
  nationality?: string;
}

// Auth Types
export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: User;
}

// Search Types
export interface FlightSearchParams {
  origin: string;
  destination: string;
  departureDate: string;
  returnDate?: string;
  passengers: number;
  cabinClass: 'ECONOMY' | 'PREMIUM_ECONOMY' | 'BUSINESS' | 'FIRST';
  tripType: 'ONE_WAY' | 'ROUND_TRIP' | 'MULTI_CITY';
}

export interface HotelSearchParams {
  city: string;
  checkIn: string;
  checkOut: string;
  rooms: number;
  adults: number;
  children: number;
}

export interface TrainSearchParams {
  origin: string;
  destination: string;
  departureDate: string;
  passengers: number;
  travelClass: 'SLEEPER' | 'AC_3_TIER' | 'AC_2_TIER' | 'AC_FIRST' | 'CHAIR_CAR';
}

export interface BusSearchParams {
  origin: string;
  destination: string;
  departureDate: string;
  passengers: number;
}

// Flight Types
export interface Flight {
  id: string;
  airline: string;
  airlineLogo: string;
  flightNumber: string;
  origin: Airport;
  destination: Airport;
  departureTime: string;
  arrivalTime: string;
  duration: number;
  stops: number;
  price: number;
  currency: string;
  cabinClass: string;
  seatsAvailable: number;
}

export interface Airport {
  code: string;
  name: string;
  city: string;
  country: string;
}

// Hotel Types
export interface Hotel {
  id: string;
  name: string;
  description: string;
  address: string;
  city: string;
  country: string;
  rating: number;
  reviewCount: number;
  amenities: string[];
  images: string[];
  pricePerNight: number;
  currency: string;
  roomsAvailable: number;
}

export interface Room {
  id: string;
  hotelId: string;
  name: string;
  description: string;
  maxOccupancy: number;
  bedType: string;
  amenities: string[];
  price: number;
  currency: string;
  available: boolean;
}

// Cart Types
export interface CartItem {
  id: string;
  type: 'FLIGHT' | 'HOTEL' | 'TRAIN' | 'BUS';
  itemId: string;
  details: Flight | Hotel | Train | Bus;
  quantity: number;
  price: number;
  currency: string;
  addOns: AddOn[];
}

export interface AddOn {
  id: string;
  type: 'SEAT' | 'MEAL' | 'BAGGAGE' | 'INSURANCE';
  name: string;
  description: string;
  price: number;
}

export interface Cart {
  id: string;
  userId: string;
  items: CartItem[];
  totalAmount: number;
  currency: string;
  createdAt: string;
  updatedAt: string;
}

// Train Types
export interface Train {
  id: string;
  trainNumber: string;
  trainName: string;
  origin: Station;
  destination: Station;
  departureTime: string;
  arrivalTime: string;
  duration: number;
  price: number;
  currency: string;
  travelClass: string;
  seatsAvailable: number;
}

export interface Station {
  code: string;
  name: string;
  city: string;
}

// Bus Types
export interface Bus {
  id: string;
  operator: string;
  busType: string;
  origin: string;
  destination: string;
  departureTime: string;
  arrivalTime: string;
  duration: number;
  price: number;
  currency: string;
  seatsAvailable: number;
  amenities: string[];
}

// Booking Types
export interface Booking {
  id: string;
  userId: string;
  bookingReference: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
  items: BookingItem[];
  travelers: Traveler[];
  totalAmount: number;
  currency: string;
  paymentStatus: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';
  createdAt: string;
  updatedAt: string;
}

export interface BookingItem {
  id: string;
  type: 'FLIGHT' | 'HOTEL' | 'TRAIN' | 'BUS';
  itemDetails: object;
  price: number;
  addOns: AddOn[];
}

// Payment Types
export interface PaymentRequest {
  bookingId: string;
  paymentMethod: 'CARD' | 'UPI' | 'NET_BANKING' | 'WALLET';
  cardDetails?: CardDetails;
}

export interface CardDetails {
  cardNumber: string;
  expiryMonth: string;
  expiryYear: string;
  cvv: string;
  cardholderName: string;
}

export interface PaymentResponse {
  id: string;
  bookingId: string;
  status: 'SUCCESS' | 'FAILED' | 'PENDING';
  transactionId: string;
  amount: number;
  currency: string;
  timestamp: string;
}

// API Response Types
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  errors?: string[];
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  hasNext: boolean;
  hasPrevious: boolean;
}
