import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { CircularProgress, Box } from '@mui/material';
import { useSelector } from 'react-redux';
import { RootState } from './store';
import MainLayout from './components/layout/MainLayout';
import AuthLayout from './components/layout/AuthLayout';
import ProtectedRoute from './components/auth/ProtectedRoute';

// Lazy load pages for code splitting
const LoginPage = lazy(() => import('./pages/auth/LoginPage'));
const RegisterPage = lazy(() => import('./pages/auth/RegisterPage'));
const ForgotPasswordPage = lazy(() => import('./pages/auth/ForgotPasswordPage'));
const DashboardPage = lazy(() => import('./pages/dashboard/DashboardPage'));
const AccountsPage = lazy(() => import('./pages/accounts/AccountsPage'));
const AccountDetailsPage = lazy(() => import('./pages/accounts/AccountDetailsPage'));
const TransfersPage = lazy(() => import('./pages/transfers/TransfersPage'));
const BeneficiaryTransferPage = lazy(() => import('./pages/transfers/BeneficiaryTransferPage'));
const OwnAccountTransferPage = lazy(() => import('./pages/transfers/OwnAccountTransferPage'));
const BeneficiariesPage = lazy(() => import('./pages/beneficiaries/BeneficiariesPage'));
const PaymentsPage = lazy(() => import('./pages/payments/PaymentsPage'));
const BillPaymentPage = lazy(() => import('./pages/payments/BillPaymentPage'));
const UPIPaymentPage = lazy(() => import('./pages/payments/UPIPaymentPage'));
const QRPaymentPage = lazy(() => import('./pages/payments/QRPaymentPage'));
const CardsPage = lazy(() => import('./pages/cards/CardsPage'));
const CardDetailsPage = lazy(() => import('./pages/cards/CardDetailsPage'));
const LoansPage = lazy(() => import('./pages/loans/LoansPage'));
const LoanApplicationPage = lazy(() => import('./pages/loans/LoanApplicationPage'));
const InvestmentsPage = lazy(() => import('./pages/investments/InvestmentsPage'));
const ProfilePage = lazy(() => import('./pages/profile/ProfilePage'));
const KYCPage = lazy(() => import('./pages/profile/KYCPage'));
const NotificationsPage = lazy(() => import('./pages/notifications/NotificationsPage'));
const SupportPage = lazy(() => import('./pages/support/SupportPage'));
const FAQPage = lazy(() => import('./pages/support/FAQPage'));

const LoadingFallback = () => (
  <Box
    display="flex"
    justifyContent="center"
    alignItems="center"
    minHeight="100vh"
  >
    <CircularProgress />
  </Box>
);

const App: React.FC = () => {
  const { isAuthenticated } = useSelector((state: RootState) => state.auth);

  return (
    <Suspense fallback={<LoadingFallback />}>
      <Routes>
        {/* Public Routes */}
        <Route element={<AuthLayout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        </Route>

        {/* Protected Routes */}
        <Route
          element={
            <ProtectedRoute>
              <MainLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/dashboard" element={<DashboardPage />} />
          
          {/* Accounts */}
          <Route path="/accounts" element={<AccountsPage />} />
          <Route path="/accounts/:accountId" element={<AccountDetailsPage />} />
          
          {/* Transfers */}
          <Route path="/transfers" element={<TransfersPage />} />
          <Route path="/transfers/own-account" element={<OwnAccountTransferPage />} />
          <Route path="/transfers/beneficiary" element={<BeneficiaryTransferPage />} />
          
          {/* Beneficiaries */}
          <Route path="/beneficiaries" element={<BeneficiariesPage />} />
          
          {/* Payments */}
          <Route path="/payments" element={<PaymentsPage />} />
          <Route path="/payments/bills" element={<BillPaymentPage />} />
          <Route path="/payments/upi" element={<UPIPaymentPage />} />
          <Route path="/payments/qr" element={<QRPaymentPage />} />
          
          {/* Cards */}
          <Route path="/cards" element={<CardsPage />} />
          <Route path="/cards/:cardId" element={<CardDetailsPage />} />
          
          {/* Loans */}
          <Route path="/loans" element={<LoansPage />} />
          <Route path="/loans/apply" element={<LoanApplicationPage />} />
          
          {/* Investments */}
          <Route path="/investments" element={<InvestmentsPage />} />
          
          {/* Profile */}
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/profile/kyc" element={<KYCPage />} />
          
          {/* Notifications */}
          <Route path="/notifications" element={<NotificationsPage />} />
          
          {/* Support */}
          <Route path="/support" element={<SupportPage />} />
          <Route path="/support/faq" element={<FAQPage />} />
        </Route>

        {/* Redirects */}
        <Route
          path="/"
          element={
            <Navigate to={isAuthenticated ? '/dashboard' : '/login'} replace />
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
};

export default App;
