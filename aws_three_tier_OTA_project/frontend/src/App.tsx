import { Routes, Route } from 'react-router-dom';
import { Toaster } from '@/components/ui/Toaster';
import Layout from '@/components/layout/Layout';
import HomePage from '@/pages/HomePage';
import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import ProfilePage from '@/pages/auth/ProfilePage';
import FlightSearchPage from '@/pages/search/FlightSearchPage';
import HotelSearchPage from '@/pages/search/HotelSearchPage';
import TrainSearchPage from '@/pages/search/TrainSearchPage';
import BusSearchPage from '@/pages/search/BusSearchPage';
import CartPage from '@/pages/cart/CartPage';
import CheckoutPage from '@/pages/checkout/CheckoutPage';
import PaymentPage from '@/pages/checkout/PaymentPage';
import BookingsPage from '@/pages/bookings/BookingsPage';
import BookingDetailsPage from '@/pages/bookings/BookingDetailsPage';
import HelpPage from '@/pages/help/HelpPage';
import NotFoundPage from '@/pages/NotFoundPage';
import ProtectedRoute from '@/components/auth/ProtectedRoute';

function App() {
  return (
    <>
      <Routes>
        <Route path="/" element={<Layout />}>
          {/* Public Routes */}
          <Route index element={<HomePage />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="register" element={<RegisterPage />} />
          <Route path="help" element={<HelpPage />} />
          
          {/* Search Routes */}
          <Route path="flights" element={<FlightSearchPage />} />
          <Route path="hotels" element={<HotelSearchPage />} />
          <Route path="trains" element={<TrainSearchPage />} />
          <Route path="buses" element={<BusSearchPage />} />
          
          {/* Protected Routes */}
          <Route element={<ProtectedRoute />}>
            <Route path="profile" element={<ProfilePage />} />
            <Route path="cart" element={<CartPage />} />
            <Route path="checkout" element={<CheckoutPage />} />
            <Route path="payment" element={<PaymentPage />} />
            <Route path="bookings" element={<BookingsPage />} />
            <Route path="bookings/:id" element={<BookingDetailsPage />} />
          </Route>
          
          {/* 404 */}
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
      <Toaster />
    </>
  );
}

export default App;
